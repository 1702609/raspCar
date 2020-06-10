import time
import RPi.GPIO as GPIO

# Next we setup the pins for use!
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(17,GPIO.OUT)
GPIO.setup(18,GPIO.OUT)
GPIO.setup(22,GPIO.OUT)
GPIO.setup(23,GPIO.OUT)


def backward():
    GPIO.output(17, True) 
    GPIO.output(18, False)
    GPIO.output(22, True)
    GPIO.output(23, False)
    time.sleep(1.5) # Go forward for 3 seconds
    GPIO.output(17, False) # Stops the wheel
    GPIO.output(22, False) # Stops the wheel

def forward():
    GPIO.output(17, False)
    GPIO.output(18, True)
    GPIO.output(22, False)
    GPIO.output(23, True)
    time.sleep(1.5) # Go backward for 3 seconds
    GPIO.output(18, False) # Stops the wheel
    GPIO.output(23, False) # Stops the wheel

def left():
    GPIO.output(17, False) 
    GPIO.output(18, True)
    time.sleep(0.6)
    GPIO.output(18, False)
    
def right():
    GPIO.output(22, False)
    GPIO.output(23, True)
    time.sleep(0.6)
    GPIO.output(23, False)



def main(direction):
    print("The arguement is "+direction)
    if direction == "f":
        print("Going forward")
        forward()
    elif direction == "l":
        print("Going left")
        left()
    elif direction == "r":
        print("Going right")
        right()
    elif direction == "b": 
        print("Going backwards")
        backward()
    else:
        print("I do not understand")
      

if __name__ == '__main__':
    import sys
    sys.exit(main(sys.argv[1]))
