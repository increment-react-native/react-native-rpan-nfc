import { NativeModules } from 'react-native';

type RpanType = {
  multiply(a: number, b: number): Promise<number>;
};

const { Rpan } = NativeModules;

export default Rpan as RpanType;
