import React, {useState, createRef} from "react";
import AsyncStorage from "@react-native-community/async-storage";
import axios from 'axios';
import {sha256} from 'react-native-sha256';
import base64 from 'base-64';
import Icon from 'react-native-vector-icons/AntDesign';

import {
    View,
    StatusBar,
    ImageBackground,
    Text,
    TextInput,
    TouchableOpacity, 
    KeyboardAvoidingView,
    Keyboard,
    } from "react-native";

import Loader from "@components/loader";
import styles from '@utils/styles';

const loginScreen = ({navigation}) => {

    const [userId, setUserId] = useState('');
    const [userPassword, setUserPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [errortext, setErrortext] = useState('');

    const passwordInputRef = createRef();

    const handleSubmitPress = () => {
        setErrortext('');
        if (!userId) {
            alert('아이디를 입력하세요.');
            return;
        }

        if (!userPassword) {
            alert('패스워드를 입력하세요.');
            return;
        }

        sha256(userPassword).then((hash) => {

            const shaPass = hash;
            const basePass = base64.encode(shaPass);
            //const [responseData, setResponseData] = useState('');

            axios({
                "method": "POST",
                "url": "http://211.240.121.123:8080/pms-server-web/app/login",
                "headers": {
                  "content-type": "nmultipart/form-data"
                }, "params": {
                  "id": userId,
                  "pass": basePass,
                  "site":"lakeedutown"
                }
              })
                .then((response) => {
                    //setResponseData(response.data);
                    //console.log(responseData);
                    if(response.data.returnCode === -1) {
                        alert("아이디 혹은 패스워드를 확인해주세요.\n");
                    } else {
                        //navigation.replace('SensorStartScreenStack');   
                        //navigation.navigate('SensorStartScreenStack');
                        navigation.replace('drawerNavigatorRoutes'); 
                    }
                })
                .catch((error) => {
                  console.log(error)
                })
    
        });

    };

    return (
        <View style={styles.root}>
        <StatusBar barStyle="light-content" backgroundColor="rgba(0,0,0,1)" />
        <KeyboardAvoidingView style={styles.background}>
            <ImageBackground
            style={styles.rect}
            imageStyle={styles.rect_imageStyle}
            source={require("@assets/images/parking.jpg")}
            >
            <View style={styles.logoColumn}>
                <View style={styles.logo}>
                <View style={styles.endWrapperFiller}></View>
                <View style={styles.text3Column}>
                    <Text style={styles.text3}>Smart Parking</Text>
                    <View style={styles.rect7}></View>
                </View>
                </View>
                <View style={styles.form}>
                    <View style={styles.usernameColumn}>
                        <View style={styles.username}>
                            <Icon name="user" style={styles.icon22} />
                            <TextInput
                                style={styles.usernameInput}
                                onChangeText={(userId) =>
                                    setUserId(userId)
                                }
                                placeholder="Enter User ID"
                                placeholderTextColor="rgba(255,255,255,1)"
                                autoCapitalize="none"
                                keyboardType="default"
                                returnKeyType="next"
                                onSubmitEditing={() =>
                                    passwordInputRef.current &&
                                    passwordInputRef.current.focus()
                                }
                                underlineColorAndroid="#f000"
                                blurOnSubmit={false}
                            />
                        </View>
                        <View style={styles.password}>
                            <Icon
                                name="lock"
                                style={styles.icon2}
                            ></Icon>
                            <TextInput
                                style={styles.passwordInput}
                                onChangeText={(userPassword) =>
                                    setUserPassword(userPassword)
                                }
                                placeholder="Enter Password"
                                placeholderTextColor="rgba(255,255,255,1)"
                                keyboardType="default"
                                ref={passwordInputRef}
                                onSubmitEditing={Keyboard.dismiss}
                                blurOnSubmit={false}
                                secureTextEntry={true}
                                underlineColorAndroid="#f000"
                                returnKeyType="next"
                            />
                        </View>
                    </View>
                <View style={styles.usernameColumnFiller}></View>
                <TouchableOpacity
                    style={styles.button}
                    activeOpacity={0.5}
                    onPress={handleSubmitPress}>
                    <Text style={styles.text2}>LOGIN</Text>
                </TouchableOpacity>
                </View>
            </View>
            
            </ImageBackground>
        </KeyboardAvoidingView>
        </View>
    );
}


export default loginScreen;
