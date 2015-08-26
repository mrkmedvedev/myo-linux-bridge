import sys
import os
import bluetooth
import thread
import subprocess

target_address = "10:92:66:FE:D6:83"

id = "00001101-0000-1000-8000-00805f9b34fb"
service_matches = bluetooth.find_service(uuid = id)

if len(service_matches) == 0:
    print "couldn't find the FooBar service"
    sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print "connecting to \"%s\" on %s" % (name, host)

sock = bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((host, port))

# This is the reading thread, similar to one in our Android app
def readThread(port):
  while True:
    # Read some bytes available from port
    bytes = sock.recv(1024)
    # Decode them as a string
    message = bytes.decode("utf-8")
    if message:
      if message[0] == '1':
        os.system("xdotool keydown --delay 40 'super'; xdotool keydown Down; xdotool keyup 'super'; xdotool keyup Down;")
      elif message[0] == '2':
        os.system("xdotool keydown --delay 40 'super'; xdotool keydown Right; xdotool keyup 'super'; xdotool keyup Right;")
      elif message[0] == '3':
        os.system("xdotool keydown --delay 40 'super'; xdotool keydown Left; xdotool keyup 'super'; xdotool keyup Left;")
      elif message[0] == '4':
        os.system("xdotool key --delay 40 'super+w';")

thread.start_new_thread( readThread, ( port, ) )

# Take raw input and write it to the port until magic symbol entered
while True:
  inp = raw_input('>')
  if len(inp):
    if inp == 'q':
      break    