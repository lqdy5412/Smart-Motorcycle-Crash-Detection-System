# i2c_lcd.py
# MicroPython I2C LCD driver (NO smbus)

from lcd_api import LcdApi
import time

class I2cLcd(LcdApi):

    def __init__(self, i2c, i2c_addr, num_lines, num_columns):
        self.i2c = i2c
        self.i2c_addr = i2c_addr
        self.backlight = 0x08
        super().__init__(num_lines, num_columns)

    def hal_write_init_nibble(self, nibble):
        self.i2c.writeto(self.i2c_addr, bytes([nibble << 4 | self.backlight]))
        self.hal_pulse_enable(nibble << 4)

    def hal_write_command(self, cmd):
        self.hal_write_byte(cmd, 0)

    def hal_write_data(self, data):
        self.hal_write_byte(data, 1)

    def hal_write_byte(self, data, mode):
        high = (data & 0xF0) | mode | self.backlight
        low = ((data << 4) & 0xF0) | mode | self.backlight
        self.i2c.writeto(self.i2c_addr, bytes([high]))
        self.hal_pulse_enable(high)
        self.i2c.writeto(self.i2c_addr, bytes([low]))
        self.hal_pulse_enable(low)

    def hal_pulse_enable(self, data):
        self.i2c.writeto(self.i2c_addr, bytes([data | 0x04]))
        time.sleep_us(1)
        self.i2c.writeto(self.i2c_addr, bytes([data & ~0x04]))
        time.sleep_us(50)
