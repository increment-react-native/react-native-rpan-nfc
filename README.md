
# react-native-react-native-rpan-nfc-agri

## Getting started

`$ npm install react-native-react-native-rpan-nfc-agri --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-rpan-nfc-agri`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeRpanNfcAgriPackage;` to the imports at the top of the file
  - Add `new RNReactNativeRpanNfcAgriPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-rpan-nfc-agri'
  	project(':react-native-react-native-rpan-nfc-agri').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-rpan-nfc-agri/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-rpan-nfc-agri')
  	```


## Usage
```javascript
import RNReactNativeRpanNfcAgri from 'react-native-react-native-rpan-nfc-agri';

// TODO: What to do with the module?
RNReactNativeRpanNfcAgri;
```
  