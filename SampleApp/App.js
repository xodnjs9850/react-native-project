// App.js backup

import React, { Component } from 'react';
import {SafeAreaView, StyleSheet, Text, View, Button, Alert} from 'react-native';

// import navigator
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';

//import screen
import introScreen from '@screens/introScreen';
import loginScreen from '@screens/loginScreen';
import drawerNavigatorRoutes from '@screens/drawerNavigatorRoutes';

const Stack = createStackNavigator();

//stack navigator for login and sign up screen
const Auth = ()=> {
  return (
    <Stack.Navigator 
      screenOptions={{headerShown: false}}>
      <Stack.Screen
        name="loginScreen"
        component={loginScreen}
        options={{headerShown: false}}
      />
    </Stack.Navigator>
  );
};

const App = ()=> {

  return (
    <NavigationContainer>

      {/* <Stack.Navigator initialRouteName="loginScreen"> */}
      <Stack.Navigator 
        screenOptions={{headerShown: false}}>
        <Stack.Screen 
          name="introScreen"
          component={introScreen}
          options={{headerShown: false}}
        />

        <Stack.Screen
          name="Auth"
          component={Auth}
          options={{headerShown: false}}
        />  

        <Stack.Screen
          name="drawerNavigatorRoutes"
          component={drawerNavigatorRoutes}
          options={{headerShown: false}}
        /> 

      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;


