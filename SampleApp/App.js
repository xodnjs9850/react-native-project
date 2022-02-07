// App.js backup
import React, { Component } from 'react';

// import navigator
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';

//import screen
import introScreen from '@screens/introScreen';
import loginScreen from '@screens/loginScreen';
import homeScreen from '@screens/homeScreen';
import SensorStartScreen from '@screens/sensorStart';
import SensorTestScreen from '@screens/sensorTestScreen';

const Stack = createStackNavigator();

const App = ()=> {

  return (
    <NavigationContainer>

      <Stack.Navigator 
        screenOptions={{headerShown: false}}>
        <Stack.Screen 
          name="introScreen"
          component={introScreen}

        />

        <Stack.Screen
          name="loginScreen"
          component={loginScreen}
        />

        <Stack.Screen
          name="homeScreen"
          component={homeScreen}
        />

        <Stack.Screen
          name="SensorStartScreen"
          
          component={SensorStartScreen}
        />

        <Stack.Screen
          name="SensorTestScreen"
          
          component={SensorTestScreen}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;


