import React from 'react';
import { Button, NativeModules } from 'react-native';
//import SampleAppModule from '@service/testModule';

const test = NativeModules.SampleAppModule;

const NewModuleButton = () => {

    const moduleTest = () => {
        test.showTest();
    }
    return (
        <Button
            title="Click to invoke your native module!"
            color="#841584"
            onPress={moduleTest}
        />
    );
};

export default NewModuleButton;