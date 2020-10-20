
# react-native-react-native-rpan-nfc

## Getting started

`$ npm install react-native-react-native-rpan-nfc --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-rpan-nfc`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeRpanNfcPackage;` to the imports at the top of the file
  - Add `new RNReactNativeRpanNfcPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-rpan-nfc'
  	project(':react-native-react-native-rpan-nfc').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-rpan-nfc/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-rpan-nfc')
  	```


## Usage
```javascript
import RNReactNativeRpanNfc from 'react-native-react-native-rpan-nfc';

// TODO: What to do with the module?
RNReactNativeRpanNfc;
```
  