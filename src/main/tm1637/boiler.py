__author__ = 'ikrokhmalyov'

#!/usr/bin/env python3

# Raspberry Pi Python 3 TM1637 quad 7-segment LED display driver examples

import tm1637, cmd, sys
from time import time, sleep, localtime

_DGT = (63,6,91,79,102,109,125,7,127,111)
CLK=27
DIO=22

tm = tm1637.TM1637(clk=CLK, dio=DIO)

class TurtleShell(cmd.Cmd):
    intro = 'Welcome to TM1637 display.\n'
    prompt = '(ready) '
    file = None

    # ----- basic turtle commands -----
    def do_num(self, arg):
        arg = parse(arg)
        tm.numbers(arg[0], arg[1])

    #def do_str(self, arg):

    def do_lisa(self, arg):
        arr = [0b00000000, 0b00000000, 0b00000000, 0b00000000,
               0b00111000, 0b00110000, 0b01101101, 0b01110111,
               0b00000000, 0b00000000, 0b00000000, 0b00000000]
        for x in range(9):
            tm.write(arr[x:x+4])
            sleep(0.2)

    def do_changed(self, arg):
        arr = [0b00000000,0b00000000, 0b00000000, 0b00000000,
               0b00111001, 0b01110100, 0b01110111, 0b01010100, 0b01111101, 0b01111001, 0b01011110,
               0b00000000,0b00000000, 0b00000000, 0b00000000]
        for x in range(12):
            tm.write(arr[x:x+4])
            sleep(0.2)

    def do_err(self, arg):
        tm.write([0b01111001, 0b01010000, 0b01010000, _DGT[int(arg[0])]])

    def do_br(self, arg):
        tm.brightness(int(arg[0]))

    def do_bye(self, arg):
        'Stop recording, close the turtle window, and exit:  BYE'
        print('Thank you for using TM1637')
        return True


def parse(arg):
    'Convert a series of zero or more numbers to an argument tuple'
    return tuple(map(int, arg.split()))

if __name__ == '__main__':
    TurtleShell().cmdloop()

## all LEDS on "88:88"
#tm.write([127, 255, 127, 127])
#
## all LEDS off
#tm.write([0, 0, 0, 0])
#
## display "0123"
#tm.write([63, 6, 91, 79])
#tm.write(bytearray([63, 6, 91, 79]))
#
## display "4567"
#tm.write([102, 109, 125, 7])
#
## set middle two segments to "12", "4127"
#tm.write([6, 91], 1)
#
## set last segment to "9", "4129"
#tm.write([111], 3)
#
## walk through all possible LED combinations
#for i in range(128):
#    tm.write([i, i | 0x80, i, i])
#
## show "AbCd"
#tm.write([119, 124, 57, 94])
#
## show "COOL"
#tm.write([0b00111001, 0b00111111, 0b00111111, 0b00111000])
#
## all LEDs off
#tm.brightness(0)
#
## all LEDs dim
#tm.brightness(1)
#
## all LEDs bright
#tm.brightness(7)
#
## converts a digit 0-0x0f to a byte representing a single segment
## use write() to render the byte on a single segment
#tm.encode_digit(0)
## 63
#
#tm.encode_digit(8)
## 127
#
#tm.encode_digit(0x0f)
## 113
#
## 15 or 0x0f generates a segment that can output a F character
#tm.encode_digit(15)
## 113
#
#tm.encode_digit(0x0f)
## 113
#
## used to convert a 1-4 length string to an array of segments
#tm.encode_string('   1')
## bytearray(b'\x00\x00\x00\x06')
#
#tm.encode_string('2   ')
## bytearray(b'[\x00\x00\x00')
#
#tm.encode_string('1234')
## bytearray(b'\x06[Of')
#
#tm.encode_string('-12-')
## bytearray(b'@\x06[@')
#
#tm.encode_string('cafe')
## bytearray(b'9wqy')
#
#tm.encode_string('CAFE')
## bytearray(b'9wqy')
#
#tm.encode_string('a')
## bytearray(b'w\x00\x00\x00')
#
#tm.encode_string('ab')
## bytearray(b'w|\x00\x00')
#
## used to convert a single character to a segment byte
#tm.encode_char('1')
## 6
#
#tm.encode_char('9')
## 111
#
#tm.encode_char('-')
## 64
#
#tm.encode_char('a')
## 119
#
#tm.encode_char('F')
## 113
#
## display "dEAd", "bEEF", "CAFE" and "bAbE"
#tm.hex(0xdead)
#tm.hex(0xbeef)
#tm.hex(0xcafe)
#tm.hex(0xbabe)
#
## show "  FF" (hex right aligned)
#tm.hex(0xff)
#
## show "   1" (numbers right aligned)
#tm.number(1)
#
## show "  12"
#tm.number(12)
#
## show " 123"
#tm.number(123)
#
## show "9999" capped at 9999
#tm.number(20000)
#
## show "  -1"
#tm.number(-1)
#
## show " -12"
#tm.number(-12)
#
## show "-123"
#tm.number(-123)
#
## show "-999" capped at -999
#tm.number(-1234)
#
## show "01:02"
#tm.numbers(1,2)
#
## show "-5:11"
#tm.numbers(-5,11)
#
## show "12:59"
#tm.numbers(12,59)
#
## show temperature "24*C"
#tm.temperature(24)


