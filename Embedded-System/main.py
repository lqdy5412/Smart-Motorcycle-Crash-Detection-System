from machine import Pin, I2C, PWM,UART
import neopixel
import time
import math
import network
import urequests
import mpu6050
import gc
import ntptime
# =========================
# TIME SYNC (NTP)
# =========================
def sync_time():
    try:
        print("⏰ Syncing time with NTP...")
        ntptime.settime()   # sets UTC time
        print("✅ Time synced:", time.localtime())
        return True
    except Exception as e:
        print("❌ NTP sync failed:", e)
        return False
# =========================
# FIREBASE
# =========================
FIREBASE_URL = "https://motorcyclealert-fb0b5-default-rtdb.firebaseio.com/accidents/latest.json"
DEVICE_SOURCE = "SMART_MOTORCYCLE_SYSTEM"

def send_firebase_alert(status, severity, source, lat, lon):
    if not wlan or not wlan.isconnected():
        print("⚠️ WiFi not available, skipping Firebase")
        return
    payload = {
        "status": status,                       # ACCIDENT / TEST
        "severity": severity,                   # LOW / MEDIUM / HIGH
        "source": source,                       # SMART_MOTORCYCLE_SYSTEM
        "timestamp": int((time.time() + 3 * 3600) * 1000)
    # Unix t ime in ms
    }
    # ===== GPS AVAILABLE =====
    if lat is not None and lon is not None:
        payload["latitude"] = lat
        payload["longitude"] = lon
        # Google Maps (Android / global)
        payload["google_maps"] = (
            f"https://www.google.com/maps?q={lat},{lon}"
        )
        # Gaode / Amap (China / backup)
        payload["gaode_maps"] = (
            f"https://uri.amap.com/marker?position={lon},{lat}"
        )
    # ===== GPS NOT AVAILABLE =====
    else:
        payload["google_maps"] = "GPS not fixed"
        payload["gaode_maps"] = "GPS not fixed"
    try:
        r = urequests.put(FIREBASE_URL, json=payload)
        print("🔥 Firebase sent:", r.text)
        r.close()
        gc.collect()   # VERY IMPORTANT for Pico memory
    except Exception as e:
        print("❌ Firebase error:", e)

# =========================
# HARDWARE SETUP
# =========================
ack_btn     = Pin(11, Pin.IN, Pin.PULL_UP)
restart_btn = Pin(4, Pin.IN, Pin.PULL_UP)
power_btn = Pin(9, Pin.IN, Pin.PULL_UP)
system_on = True
last_power_btn = 1

led    = Pin(14, Pin.OUT)
buzzer = PWM(Pin(15))

NUM_LEDS = 8
rgb = neopixel.NeoPixel(Pin(6), NUM_LEDS)

i2c = I2C(0, scl=Pin(1), sda=Pin(0), freq=100000)
imu = mpu6050.accel(i2c)

# =========================
# RGB
# =========================
def set_rgb_all(r, g, b):
    for i in range(len(rgb)):
        rgb[i] = (r, g, b)
    rgb.write()
    
# =========================
# GPS SETUP (NEO-6M)
# =========================
gps = UART(0, baudrate=9600, tx=Pin(16), rx=Pin(17), timeout=1000)

gps_lat = None
gps_lon = None
gps_fix =  False

def convert_to_degrees(raw, direction):
    if not raw or raw == "":
        return None
    d = float(raw)
    degrees = int(d // 100)
    minutes = d - (degrees * 100)
    dec = degrees + minutes / 60
    if direction in ['S', 'W']:
        dec = -dec
    return round(dec, 6)

def update_gps():
    global gps_lat, gps_lon, gps_fix
    if gps.any():
        try:
            line = gps.readline()
            if not line:
                return
            line = line.decode("utf-8", "ignore")
            if line.startswith("$GPRMC"):
                parts = line.split(",")
                if parts[2] == "A":  # Valid fix
                    lat = convert_to_degrees(parts[3], parts[4])
                    lon = convert_to_degrees(parts[5], parts[6])
                    if lat and lon:
                        gps_lat = lat
                        gps_lon = lon
                        gps_fix = True
        except:
            pass

# =========================
# LCD1602 RGB
# =========================
LCD_ADDR = 0x3E
RGB_ADDR = 0x60

def lcd_cmd(cmd):
    i2c.writeto(LCD_ADDR, bytes([0x80, cmd]))
    time.sleep_ms(2)

def lcd_data(data):
    i2c.writeto(LCD_ADDR, bytes([0x40, data]))

def lcd_init():
    time.sleep(0.05)
    lcd_cmd(0x38)
    lcd_cmd(0x39)
    lcd_cmd(0x14)
    lcd_cmd(0x70)
    lcd_cmd(0x56)
    lcd_cmd(0x6C)
    time.sleep(0.2)
    lcd_cmd(0x38)
    lcd_cmd(0x0C)
    lcd_cmd(0x01)

def lcd_print(text, line):
    lcd_cmd(0x80 if line == 0 else 0xC0)
    if len(text) > 16:
        text = text[:16]
    while len(text) < 16:
        text += " "
    for c in text:
        lcd_data(ord(c))

def set_lcd_rgb(r, g, b):
    i2c.writeto(RGB_ADDR, bytes([0x00, 0x00]))
    i2c.writeto(RGB_ADDR, bytes([0x01, 0x00]))
    i2c.writeto(RGB_ADDR, bytes([0x08, 0xAA]))
    i2c.writeto(RGB_ADDR, bytes([0x04, r]))
    i2c.writeto(RGB_ADDR, bytes([0x03, g]))
    i2c.writeto(RGB_ADDR, bytes([0x02, b]))

# =========================
# System ShutDown + System StartUp
# =========================
DEBOUNCE_MS = 300
last_toggle_time = 0

def power_pressed():
    global last_power_btn, last_toggle_time
    now = time.ticks_ms()
    cur = power_btn.value()

    pressed = False
    if last_power_btn == 1 and cur == 0:  # falling edge
        if time.ticks_diff(now, last_toggle_time) > DEBOUNCE_MS:
            pressed = True
            last_toggle_time = now

    last_power_btn = cur
    return pressed

def system_shutdown():
    print("🛑 SYSTEM SHUTDOWN")
    
    # Stop outputs
    led.value(0)
    buzzer.duty_u16(0)

    set_rgb_all(0, 0, 0)

    set_lcd_rgb(0, 0, 0)
    lcd_print("SYSTEM OFF", 0)
    lcd_print("Press POWER", 1)

def system_startup():
    print("▶️ SYSTEM STARTING")

    lcd_init()
    lcd_print("SYSTEM READY", 0)
    lcd_print("Monitoring...", 1)
    set_lcd_rgb(0, 255, 0)

    set_rgb_all(0, 255, 0)

    # Reconnect WiFi

    if wlan and wlan.isconnected():

        print("WiFi already connected")
    else:
        reconnect_wifi()
# =========================
# WIFI + IFTTT + LOCATION
# =========================
SSID = "iPhone"
PASSWORD = "12345678"

IFTTT_URL = "http://maker.ifttt.com/trigger/crash_alert/with/key/kCbnEHxZd1z9EC-aRoOxtw1ee_n4KRnauVeT_UGNhJ6"

IFTTT_SAFE_URL = "http://maker.ifttt.com/trigger/driver_safe/with/key/kCbnEHxZd1z9EC-aRoOxtw1ee_n4KRnauVeT_UGNhJ6"

google_maps_link = None
gaode_maps_link = None

def send_ifttt_notification(value1, value2):
    if not wlan or not wlan.isconnected():
        print("⚠️ WiFi not available, skipping IFTTT")
        return

    data = {
        "value1": value1,
        "value2": value2
    }
    try:
        r = urequests.post(IFTTT_URL, json=data)
        print("IFTTT response:", r.status_code)
        r.close()
    except Exception as e:
        print("IFTTT error:", e)

def send_driver_safe():
    if not wlan or not wlan.isconnected():
        print("⚠️ WiFi not available, skipping driver safe message")
        return

    try:
        r = urequests.post(IFTTT_SAFE_URL, json={})
        r.close()
        print("Driver safe notification sent")
    except Exception as e:
        print("IFTTT error:", e)

# =========================
# ACCIDENT DETECTION
# =========================
ACC_HIGH       = 18.0    # Severe impact
ACC_MEDIUM     = 14.5
GYRO_MEDIUM    = 150.0
GYRO_VERY_HIGH = 220.0
FALL_TIME_MS   = 100.0

def acc_magnitude():
    v = imu.get_values()
    ax, ay, az = v["AcX"], v["AcY"], v["AcZ"]
    return math.sqrt(ax*ax + ay*ay + az*az)

def gyro_magnitude():
    gx, gy, gz = imu.read_gyro()
    return math.sqrt(gx*gx + gy*gy + gz*gz)

# =========================
# WIFI CONNECT
# =========================
def connect_wifi(timeout_sec=15):
    print("Connecting to WiFi...")
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    wlan.connect(SSID, PASSWORD)

    start = time.time()
    while not wlan.isconnected():
        if time.time() - start > timeout_sec:
            print("⚠️ WiFi connection timed out")
            return None
        time.sleep(0.5)

    print("✅ WiFi connected:", wlan.ifconfig()[0])
    return wlan

def reconnect_wifi(timeout_sec=15):
    global wlan

    print("🔄 Reconnecting WiFi...")
    
    # If wlan does not exist, create it
    if wlan is None:
        wlan = network.WLAN(network.STA_IF)

    wlan.active(False)
    time.sleep(1)
    wlan.active(True)
    wlan.connect(SSID, PASSWORD)

    start = time.time()
    while not wlan.isconnected():
        if time.time() - start > timeout_sec:
            print("⚠️ WiFi reconnection failed")
            return False
        time.sleep(0.5)

    print("✅ WiFi reconnected:", wlan.ifconfig()[0])
    return True

lcd_init()
lcd_print("System Started",0)
set_lcd_rgb(255,255,255)

set_rgb_all(50,50,50)
# ---- CONNECT WIFI WITH TIMEOUT ----
wlan = connect_wifi(timeout_sec=15)
if wlan:
    sync_time()

# =========================
# MAIN SYSTEM LOOP
# =========================

while True:
    
    # ---------- GLOBAL POWER CHECK ----------
    if power_pressed():
        system_on = not system_on
        if system_on:
            system_startup()
        else:
            system_shutdown()

    if not system_on:
        time.sleep(0.1)
        continue
    
    # ---------- SYSTEM READY ----------
    lcd_print("SYSTEM READY", 0)
    lcd_print("Monitoring...", 1)
    set_lcd_rgb(0, 255, 0)

    set_rgb_all(0, 255, 0)

    led.value(0)
    buzzer.duty_u16(0)

    last_lcd_update = 0
    crash_triggered = False

    # =========================
    # ---- MONITORING LOOP ----
    # =========================
    while True:

        # ----- POWER BUTTON (MONITORING) -----
        if power_pressed():
            system_on = False
            break

        acc  = acc_magnitude()
        gyro = gyro_magnitude()

        print("ACC:", acc, "GYRO:", gyro)

        if power_pressed():
            system_on = False
            break

        if time.ticks_ms() - last_lcd_update > 500:
            lcd_print("ACC:{:.1f} m/s2".format(acc), 0)
            lcd_print("GYR:{:.1f} d/s".format(gyro), 1)
            last_lcd_update = time.ticks_ms()
            
        # ---- ACCIDENT DETECTION ----
        if acc > ACC_HIGH:
            crash_triggered = True
            print("Case 1: Impact")
            break

        if acc > ACC_MEDIUM and gyro > GYRO_MEDIUM:
            crash_triggered = True
            print("Case 2: Impact + rotation")
            break

        if gyro > GYRO_VERY_HIGH:
            t0 = time.ticks_ms()
            while time.ticks_diff(time.ticks_ms(), t0) < FALL_TIME_MS:
                if power_pressed():
                    system_on = False
                    break

                if gyro_magnitude() < GYRO_VERY_HIGH:
                    break

                time.sleep(0.01)

            else:
                crash_triggered = True
                print("Case 3: Bike fall")
                break

        # Non-blocking sleep (100 ms total)
        for _ in range(10):          # 10 × 10ms = 100ms
            if power_pressed():
                system_on = False
                break
            time.sleep(0.01)

        if not system_on:
            break
    # ---------- HANDLE POWER OFF ----------
    if not system_on:
        system_shutdown()
        continue

    if not crash_triggered:
        continue

    # =========================
    # ---- CRASH HANDLING ----
    # =========================
    lcd_print("CRASH DETECTED", 0)
    lcd_print("Press ACK", 1)
    set_lcd_rgb(255, 0, 0)

    set_rgb_all(255, 0, 0)

    led.value(1)
    buzzer.freq(1000)
    buzzer.duty_u16(30000)

    # ---- GPS UPDATE ----
    for _ in range(20):
        update_gps()
        if gps_fix:
            break
        time.sleep(0.1)

    if gps_fix:
        google_maps_link = f"https://www.google.com/maps?q={gps_lat},{gps_lon}"
        gaode_maps_link  = f"https://uri.amap.com/marker?position={gps_lon},{gps_lat}"
    else:
        google_maps_link = "GPS not fixed"
        gaode_maps_link  = "GPS not fixed"

    send_ifttt_notification(google_maps_link, gaode_maps_link)

    # ---- SEND TO FIREBASE ----
    send_firebase_alert(
        status="ACCIDENT",
        severity="HIGH" if acc > ACC_HIGH else "MEDIUM",
        source=DEVICE_SOURCE,
        lat=gps_lat if gps_fix else None,
        lon=gps_lon if gps_fix else None
    )
    # ---- BLINK LED + BUZZER UNTIL ACK ----
    while ack_btn.value() == 1:

        # ON phase
        led.value(1)
        buzzer.duty_u16(30000)
        for _ in range(50):          # 50 × 10ms = 0.5s
            if ack_btn.value() == 0:
                break
            time.sleep(0.01)
            
        # OFF phase
        led.value(0)
        buzzer.duty_u16(0)
        for _ in range(50):
            if ack_btn.value() == 0:
                break
            time.sleep(0.01)

    time.sleep(0.3)
    send_driver_safe()

    buzzer.duty_u16(0)
    led.value(0)

    lcd_print("ACK RECEIVED", 0)
    lcd_print("Press RESTART", 1)
    set_lcd_rgb(0, 0, 255)

    set_rgb_all(0, 0, 255)

    # ---- WAIT FOR RESTART ----
    while restart_btn.value() == 1:
        time.sleep(0.1)

    gps_fix = False
    gps_lat = None
    gps_lon = None

    lcd_print("Restarting", 0)
    lcd_print("System", 1)
    reconnect_wifi(timeout_sec=15)

    time.sleep(0.3)
