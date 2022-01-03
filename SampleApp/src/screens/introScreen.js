import React, {useState, useEffect} from "react";
import { Text, View, ImageBackground, ActivityIndicator } from "react-native";
import AsyncStorage from "@react-native-community/async-storage";

import styles from '@utils/styles'

const introScreen = ({navigation}) => {

    const [animating, setAnimating] = useState(true);

    useEffect(() => {
        setTimeout(() => {
            setAnimating(false);
            // Check user id
            AsyncStorage.getItem('user_id').then((value) =>
                navigation.replace(
                    value === null ? 'Auth' : 'drawerNavigationRoutes'
                ), 
            );
        } , 3000);
    }, []);

    return (
        <View style={styles.root}>
            <ImageBackground
            style={styles.rect}
            imageStyle={styles.rect_imageStyle}
            source={require("@assets/images/parking.jpg")}> 
            <View style={styles.introScreen}>
                <Text style={styles.logoText}>Smart Parking</Text>
                <ActivityIndicator
                    animating={animating}
                    color="#FFFFFF"
                    size="large"
                    style={styles.activityIndicator}
                />
            </View>
            </ImageBackground> 
        </View>
    );
};
export default introScreen;
