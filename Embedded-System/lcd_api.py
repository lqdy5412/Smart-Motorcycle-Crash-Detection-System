# lcd_api.py
# MicroPython LCD API

import time

class LcdApi:
    LCD_CLR = 0x01
    LCD_HOME = 0x02

    LCD_ENTRY_MODE = 0x04
    LCD_ENTRY_INC = 0x02

    LCD_ON_CTRL = 0x08
    LCD_ON_DISPLAY = 0x04

    LCD_FUNCTION = 0x20
    LCD_FUNCTION_2LINES = 0x08
    LCD_FUNCTION_5x8DOTS = 0x00

    LCD_SET_DDRAM = 0x80

    def __init__(self, num_lines, num_columns):
        self.num_lines = num_lines
        self.num_columns = num_columns
        self.cursor_x = 0
        self.cursor_y = 0
        self.init_lcd()

    def init_lcd(self):
        time.sleep_ms(20)
        self.hal_write_init_nibble(0x03)
        time.sleep_ms(5)
        self.hal_write_init_nibble(0x03)
        time.sleep_ms(1)
        self.hal_write_init_nibble(0x03)
        self.hal_write_init_nibble(0x02)

        self.hal_write_command(
            self.LCD_FUNCTION |
            self.LCD_FUNCTION_2LINES |
            self.LCD_FUNCTION_5x8DOTS
        )

        self.hal_write_command(self.LCD_ON_CTRL | self.LCD_ON_DISPLAY)
        self.clear()
        self.hal_write_command(self.LCD_ENTRY_MODE | self.LCD_ENTRY_INC)

    def clear(self):
        self.hal_write_command(self.LCD_CLR)
        time.sleep_ms(2)
        self.cursor_x = 0
        self.cursor_y = 0

    def move_to(self, x, y):
        addr = x + 0x40 * y
        self.hal_write_command(self.LCD_SET_DDRAM | addr)
        self.cursor_x = x
        self.cursor_y = y

    def putchar(self, char):
        self.hal_write_data(ord(char))
        self.cursor_x += 1
        if self.cursor_x >= self.num_columns:
            self.cursor_x = 0
            self.cursor_y = (self.cursor_y + 1) % self.num_lines
            self.move_to(self.cursor_x, self.cursor_y)

    def putstr(self, string):
        for char in string:
            self.putchar(char)

    # These must be implemented by subclass
    def hal_write_command(self, cmd):
        raise NotImplementedError

    def hal_write_data(self, data):
        raise NotImplementedError

    def hal_write_init_nibble(self, nibble):
        raise NotImplementedError
