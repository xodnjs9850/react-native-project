import React from "react";

import { createStackNavigator } from "@react-navigation/stack";
import { createDrawerNavigator } from "@react-navigation/drawer";

import NavigationDrawerHeader from "@components/navigationDrawerHeader";

// import Screen
import HomeScreen from "@screens/homeScreen";
import SensorTestScreen from "@screens/sensorTestScreen";
import SensorStart from "@screens/sensorStart";

const Stack = createStackNavigator();
const Drawer = createDrawerNavigator();

const homeScreenStack = ({navigation}) => {

    return (
        <Stack.Navigator
            screenOptions={{headerShown: false}}>
            <Stack.Screen
                name="HomeScreen"
                component={HomeScreen}
                options={{
                    tittle: 'Home',
                    headerStyle: {
                        backgroundColor: '#307ecc',
                    },
                    headerTintColor: '#fff',
                    headerTitleStyle: {
                        fontWeight: 'bold',
                    },
                }}
            />
        </Stack.Navigator>
    );
};

const SensorTestScreenStack = ({navigation}) => {
    return (
      <Stack.Navigator
          screenOptions={{headerShown: false}}>
          <Stack.Screen
              name="SensorTestScreen"
              component={SensorTestScreen}
              options={{
                  tittle: 'Sensor Test',
                  headerStyle: {
                      backgroundColor: '#307ecc',
                  },
                  headerTintColor: '#fff',
                  headerTitleStyle: {
                      fontWeight: 'bold',
                  },
              }}
          />
      </Stack.Navigator>
    );
};

const SensorStartScreenStack = ({navigation}) => {
  return (
    <Stack.Navigator
        screenOptions={{headerShown: false}}>
        <Stack.Screen
            name="SensorStartScreenStack"
            component={SensorStart}
            options={{
                tittle: 'Sensor Start',
                headerStyle: {
                    backgroundColor: '#307ecc',
                },
                headerTintColor: '#fff',
                headerTitleStyle: {
                    fontWeight: 'bold',
                },
            }}
        />
    </Stack.Navigator>
  );
};

const drawerNavigatorRoutes = (props) => {
    return (
      <Drawer.Navigator
        drawerContentOptions={{
          activeTintColor: '#cee1f2',
          color: '#cee1f2',
          itemStyle: {marginVertical: 5, color: 'white'},
          labelStyle: {
            color: '#d8d8d8',
          },
        }}
        screenOptions={{headerShown: false}}
        >
        {/* <Drawer.Screen
          name="homeScreenStack"
          options={{drawerLabel: 'Home Screen'}}
          component={homeScreenStack}
        /> */}

        <Drawer.Screen
          name="SensorTestScreenStack"
          options={{drawerLabel: 'Sensor Test Screen'}}
          component={SensorTestScreenStack}
        />

        <Drawer.Screen
          name="SensorStartScreenStack"
          options={{drawerLabel: 'Sensor Test Screen'}}
          component={SensorStartScreenStack}
        />


      </Drawer.Navigator>
    );
  };

  export default drawerNavigatorRoutes;
  