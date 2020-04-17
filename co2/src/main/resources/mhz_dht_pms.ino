#include <DHT.h>
#include <DHT_U.h>

#include <SoftwareSerial.h>
SoftwareSerial pmsSerial(4, 5);

#define DHTPIN 2     // what pin we're connected to
#define DHTTYPE DHT22   // DHT 22  (AM2302)
DHT dht(DHTPIN, DHTTYPE); //// Initialize DHT sensor for normal 16mhz Arduino


SoftwareSerial co2SensorSerial(9, 8); 


// CO2 sensor data structures:
byte cmd[9] = {0xFF,0x01,0x86,0x00,0x00,0x00,0x00,0x00,0x79};
byte abs_off[9] = {0xFF,0x01,0x86,0x00,0x00,0x00,0x00,0x00,0x86};
//co2SensorSerial.write(cmd, 9) 
unsigned char co2SensorResponse[9];
byte crc = 0;

//Variables
int chk;
float hum;  //Stores humidity value
float temp; //Stores temperature value

void setup() {
  // our debugging output
  Serial.begin(9600);
 
  // sensor baud rate is 9600
  pmsSerial.begin(9600);
  co2SensorSerial.begin(9600);
  dht.begin();
}
 
struct pms5003data {
  uint16_t framelen;
  uint16_t pm10_standard, pm25_standard, pm100_standard;
  uint16_t pm10_env, pm25_env, pm100_env;
  uint16_t particles_03um, particles_05um, particles_10um, particles_25um, particles_50um, particles_100um;
  uint16_t unused;
  uint16_t checksum;
};
 
struct pms5003data data;
int count = 0;
    
void loop() {
   
  pmsSerial.listen();
  
  //delay(1000); //Delay 2 sec.
    
  if (readPMSdata(&pmsSerial)) {
    Serial.print("PM2.5: ");
    Serial.print(data.pm25_standard);
    // reading data was successful!
    //Serial.println();
    //Serial.println("---------------------------------------");
    //Serial.println("");
    //Serial.print("PM 1.0: "); Serial.print(data.pm10_standard);
    //Serial.print("Concentration Units (standard) PM 2.5: "); Serial.println(data.pm25_standard);
    //Serial.print("\t\tPM 10: "); Serial.println(data.pm100_standard);
    //Serial.println("---------------------------------------");
    //Serial.println("Concentration Units (environmental)");
    //Serial.print("PM 1.0: "); Serial.print(data.pm10_env);
    //Serial.print("Concentration Units (environmental) PM 2.5: "); Serial.println(data.pm25_env);
    //Serial.print("\t\tPM 10: "); Serial.println(data.pm100_env);
    //Serial.println("---------------------------------------");
    //Serial.print("Particles > 0.3um / 0.1L air:"); Serial.println(data.particles_03um);
    //Serial.print("Particles > 0.5um / 0.1L air:"); Serial.println(data.particles_05um);
    //Serial.print("Particles > 1.0um / 0.1L air:"); Serial.println(data.particles_10um);
    //Serial.print("Particles > 2.5um / 0.1L air:"); Serial.println(data.particles_25um);
    //Serial.print("Particles > 5.0um / 0.1L air:"); Serial.println(data.particles_50um);
    //Serial.print("Particles > 10.0 um / 0.1L air:"); Serial.println(data.particles_100um);
    //Serial.println("---------------------------------------");
    delay(100);
    hum = dht.readHumidity();
    temp = dht.readTemperature();
    //Print temp and humidity values to serial monitor
    Serial.print(", Humidity: ");
    Serial.print(hum);
    Serial.print(", Temp: ");
    Serial.print(temp);

    delay(100);  
    co2SensorSerial.listen();
    delay(100); 
    readCo2SensorValueToCo2Response();

    Serial.print(", CO2: ");
    //clearDisplay();
    if(validateCo2Response()) {
      int co2Value = (256 * (unsigned int) co2SensorResponse[2]) + (unsigned int) co2SensorResponse[3];
      Serial.print(co2Value);
    } else {
      Serial.print("ERROR");
    }
    Serial.println()
    delay(5000); 
  } else {
    delay(1000); 
  }

   
  
}
 
boolean readPMSdata(Stream *s) {
  if (! s->available()) {
    return false;
  }
  
  // Read a byte at a time until we get to the special '0x42' start-byte
  if (s->peek() != 0x42) {
    s->read();
    return false;
  }
 
  // Now read all 32 bytes
  if (s->available() < 32) {
    return false;
  }
    
  uint8_t buffer[32];    
  uint16_t sum = 0;
  s->readBytes(buffer, 32);
 
  // get checksum ready
  for (uint8_t i=0; i<30; i++) {
    sum += buffer[i];
  }
 
  // debugging
  /*for (uint8_t i=2; i<32; i++) {
    Serial.print("0x"); Serial.print(buffer[i], HEX); Serial.print(", ");
  }
  Serial.println();
  */
  
  
  // The data comes in endian'd, this solves it so it works on all platforms
  uint16_t buffer_u16[15];
  for (uint8_t i=0; i<15; i++) {
    buffer_u16[i] = buffer[2 + i*2 + 1];
    buffer_u16[i] += (buffer[2 + i*2] << 8);
  }
 
  // put it into a nice struct :)
  memcpy((void *)&data, (void *)buffer_u16, 30);
 
  if (sum != data.checksum) {
    Serial.println("Checksum failure");
    return false;
  }
  // success!
  return true;
}


bool validateCo2Response() {
  crc = 0;
  for (int i = 1; i < 8; i++) {
    crc += co2SensorResponse[i];
  }
  crc = 256 - crc;
  //crc++;
  bool valid = co2SensorResponse[0] == 0xFF && co2SensorResponse[1] == 0x86 && co2SensorResponse[8] == crc;
  if(!valid) {
    Serial.println("CRC error: " + String(crc) + "/"+ String(co2SensorResponse[8]));
  }
  return valid; 
}
  
void   readCo2SensorValueToCo2Response() {
  co2SensorSerial.write(cmd, 9);
  memset(co2SensorResponse, 0, 9);
  co2SensorSerial.readBytes(co2SensorResponse, 9);
}
