import { NativeModules, DeviceEventEmitter } from 'react-native';
import { ScannerEvents } from './Events';

const rpanScannerManager = NativeModules.RNReactNativeRpanNfc;

let instance = null;

export class Scanner {
  constructor () {
    if (!instance) {
      instance = this;
      this.opened = false;
      this.deferReading = false;
      this.oncallbacks = [];
      this.config = {};

      DeviceEventEmitter.addListener('TagEvent', this.handleTagEvent.bind(this));
      DeviceEventEmitter.addListener('TagsEvent', this.handleTagsEvent.bind(this));
      DeviceEventEmitter.addListener('RFIDStatusEvent', this.handleStatusEvent.bind(this));
    }
  }

  handleStatusEvent (event) {
    console.log('RFID status event ' + event.RFIDStatusEvent);
    if (event.RFIDStatusEvent === 'opened') {
      this.opened = true;
      if (this.deferReading) {
        rfidScannerManager.read(this.config);
        this.deferReading = false;
      }
    } else if (event.RFIDStatusEvent === 'closed') {
      this.opened = false;
    }
  }

  handleTagEvent (tag) {
    if (this.oncallbacks.hasOwnProperty(ScannerEvents.TAG)) {
      this.oncallbacks[ScannerEvents.TAG].forEach((callback) => {
        callback(tag);
      });
    }
  }

  handleTagsEvent (tags) {
    if (this.oncallbacks.hasOwnProperty(ScannerEvents.TAGS)) {
      this.oncallbacks[ScannerEvents.TAGS].forEach((callback) => {
        callback(tags);
      });
    }
  }

  init () {
    rpanScannerManager.init();
  }

  read (config = {}) {
    this.config = config;

    if (this.opened) {
      rpanScannerManager.read(this.config);
    } else {
      this.deferReading = true;
    }
  }

  reconnect () {
    rpanScannerManager.reconnect();
  }

  cancel () {
    rpanScannerManager.cancel();
  }

  connect(value, callback) {
    rpanScannerManager.connect(value, callback);
  }

  shutdown () {
    rpanScannerManager.shutdown();
  }

  power (value) {
    rpanScannerManager.power(value);
  }

  getBattery(){
    rpanScannerManager.getBattery();
  }

  connectDevice(){  
    rpanScannerManager.connectDevice();
  }

  disconnect(){
    rpanScannerManager.disconnect();
  }

  devices(callback){
    console.log(callback)
    rpanScannerManager.devices(callback);
  }


  on (event, callback) {
  }

  removeon (event, callback) {
  }

  hason (event, callback) {
  }
}

export default new Scanner();
