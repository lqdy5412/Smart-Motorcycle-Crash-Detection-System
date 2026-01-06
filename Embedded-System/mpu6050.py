# mpu6050.py
# MicroPython driver for the MPU6050 (I2C)
# Returns accelerometer in m/s^2 by default
# Author: adapted for Husam Abdullah project

from machine import I2C
import utime

# MPU6050 default I2C address
MPU6050_ADDR = 0x68

# Registers
PWR_MGMT_1   = 0x6B
SMPLRT_DIV   = 0x19
CONFIG       = 0x1A
GYRO_CONFIG  = 0x1B
ACCEL_CONFIG = 0x1C
INT_ENABLE   = 0x38
ACCEL_XOUT_H = 0x3B
TEMP_OUT_H   = 0x41
GYRO_XOUT_H  = 0x43
WHO_AM_I     = 0x75

# Constants
ACCEL_SCALE_MODIFIER_2G = 16384.0
G_TO_MS2 = 9.80665

class MPU6050:
    def __init__(self, i2c: I2C, addr: int = MPU6050_ADDR):
        self.i2c = i2c
        self.addr = addr
        # Wake up the sensor (clear sleep bit)
        try:
            self._write_reg(PWR_MGMT_1, 0x00)
            utime.sleep_ms(50)
            # set sample rate divider to default (1 kHz/(1+SMPLRT_DIV))
            self._write_reg(SMPLRT_DIV, 0x07)
            # set accel range to +-2g
            self._write_reg(ACCEL_CONFIG, 0x00)
            # set gyro range to +-250 deg/s
            self._write_reg(GYRO_CONFIG, 0x00)
            utime.sleep_ms(20)
        except Exception as e:
            # ignore for simulation, raise on real hardware if needed
            raise e

    # low level helpers
    def _write_reg(self, reg, value):
        self.i2c.writeto_mem(self.addr, reg, bytes([value]))

    def _read_reg(self, reg, nbytes):
        return self.i2c.readfrom_mem(self.addr, reg, nbytes)

    def _read_int16(self, reg):
        b = self._read_reg(reg, 2)
        val = (b[0] << 8) | b[1]
        if val & 0x8000:
            val = -((65535 - val) + 1)
        return val

    # public methods
    def who_am_i(self):
        try:
            return self._read_reg(WHO_AM_I, 1)[0]
        except:
            return None

    def read_raw_accel(self):
        """Return raw accelerometer readings as tuple (ax, ay, az)."""
        ax = self._read_int16(ACCEL_XOUT_H)
        ay = self._read_int16(ACCEL_XOUT_H + 2)
        az = self._read_int16(ACCEL_XOUT_H + 4)
        return ax, ay, az

    def read_accel(self):
        """
        Return accelerometer in m/s^2 as (ax, ay, az).
        Uses ±2g scale by default (modify if you change sensor config).
        """
        ax_raw, ay_raw, az_raw = self.read_raw_accel()
        ax_g = ax_raw / ACCEL_SCALE_MODIFIER_2G
        ay_g = ay_raw / ACCEL_SCALE_MODIFIER_2G
        az_g = az_raw / ACCEL_SCALE_MODIFIER_2G
        # convert g to m/s^2
        return ax_g * G_TO_MS2, ay_g * G_TO_MS2, az_g * G_TO_MS2

    def read_temp(self):
        """Return temperature in degrees Celsius."""
        raw = self._read_int16(TEMP_OUT_H)
        # per datasheet: Temp in °C = (raw / 340) + 36.53
        return (raw / 340.0) + 36.53

    def read_gyro(self):
        """Return gyro in deg/s as (gx, gy, gz) with ±250 dps config."""
        gx = self._read_int16(GYRO_XOUT_H)
        gy = self._read_int16(GYRO_XOUT_H + 2)
        gz = self._read_int16(GYRO_XOUT_H + 4)
        # gyro scale modifier for ±250 dps is 131 LSB/(deg/s)
        return gx / 131.0, gy / 131.0, gz / 131.0

    def get_values(self):
        """
        Convenience method returning a dictionary of values:
        {'AcX':..., 'AcY':..., 'AcZ':..., 'Temp':..., 'GyX':..., 'GyY':..., 'GyZ':...}
        Accelerations are in m/s^2.
        """
        ax, ay, az = self.read_accel()
        temp = self.read_temp()
        gx, gy, gz = self.read_gyro()
        return {
            'AcX': ax,
            'AcY': ay,
            'AcZ': az,
            'Temp': temp,
            'GyX': gx,
            'GyY': gy,
            'GyZ': gz
        }

# Backwards-friendly small wrapper name used in examples
def accel(i2c, addr=MPU6050_ADDR):
    return MPU6050(i2c, addr)

