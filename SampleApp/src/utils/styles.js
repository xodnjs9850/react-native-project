import { StyleSheet } from "react-native";

const styles = StyleSheet.create({

    root: {
      flex: 1,
      backgroundColor: "rgb(255,255,255)"
    },
    headerX: {
      height: 80,
      elevation: 15,
      shadowOffset: {
        height: 7,
        width: 1
      },
      shadowColor: "rgba(0,0,0,1)",
      shadowOpacity: 0.1,
      shadowRadius: 5
    },
    body: {
      flex: 1,
      margin: 5,
    },
    headline: {
      height: 246,
      overflow: "hidden"
    },
    image: {
      flex: 1
    },
    image_imageStyle: {},
    overlay: {
      backgroundColor: "rgba(30,26,26,0.4)",
      flex: 1
    },
    scienceChannel: {
      color: "rgba(255,255,255,1)",
      fontSize: 24,
      marginTop: 43,
      alignSelf: "center"
    },
    following: {
      width: 90,
      height: 40,
      backgroundColor: "rgba(255,255,255,1)",
      borderRadius: 5,
      justifyContent: "center",
      marginTop: 28,
      alignSelf: "center"
    },
    text: {
      color: "rgba(31,178,204,1)",
      fontSize: 14,
      alignSelf: "center"
    },
    followers: {
      color: "rgba(255,255,255,1)",
      fontSize: 16,
      marginTop: 39,
      alignSelf: "center"
    },
    scrollArea: {
      height: 413
    },
    scrollArea_contentContainerStyle: {
      height: 413
    },
    scrollViewEntry: {
      height: 100
    },
    scrollViewEntry4: {
      width: 360,
      height: 100
    },
    scrollViewEntry2: {
      width: 360,
      height: 100
    },
    scrollViewEntry3: {
      width: 360,
      height: 100
    },
    
    ActivityIndicatorWrapper: {
      backgroundColor: '#FFFFFF',
      height: 100,
      width: 100,
      borderRadius: 10,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-around',
    },

    activityIndicator: {
        alignItems: 'center',
        height: 80,
    },
        
    background: {
      flex: 1
    },
    rect: {
      flex: 1
    },
    rect_imageStyle: {},
    logo: {
      width: 220,
      height: 100,
      alignSelf: "center",
      alignItems: "center",
    },
    introScreen: {
      justifyContent: "center",
        alignItems: "center",
    },
    logoText: {
      fontSize: 40,
      alignItems: "center",
      justifyContent: "center",
      marginTop: 250,
    },
    endWrapperFiller: {
      flex: 1
    },
    text3: {
      color: "rgba(0,0,0,1)",
      fontSize: 30,
      marginBottom: 4,
      alignItems: "center",
      justifyContent: "center",
    },
    rect7: {
      height: 8,
      backgroundColor: "#25cdec",
      marginRight: 4
    },
    text3Column: {
      marginBottom: 6,
      marginLeft: 2,
      marginRight: -1
    },
    form: {
      height: 230,
      marginTop: 50
    },
    username: {
      height: 50,
      width: 250,
      backgroundColor: "rgba(251,247,247,0.25)",
      borderRadius: 5,
      flexDirection: "row",
      alignSelf: "center"
      //marginLeft: 7,
    },
    icon22: {
      color: "rgba(0,0,0,1)",
      fontSize: 30,
      marginLeft: 20,
      alignSelf: "center"
    },
    usernameInput: {
      height: 50,
      width: 250,
      color: "rgba(0,0,0,1)",
      //marginLeft: 8,
    },
    password: {
      height: 50,
      width: 250,
      backgroundColor: "rgba(253,251,251,0.25)",
      borderRadius: 5,
      flexDirection: "row",
      marginTop: 27,
      alignSelf: "center"
      //marginLeft: 7,
      
    },
    icon2: {
      color: "rgba(0,0,0,1)",
      fontSize: 33,
      marginLeft: 20,
      alignSelf: "center"
    },
    passwordInput: {
      height: 50,
      width: 250,
      color: "rgba(0,0,0,1)",
      //marginLeft: 8,
    },
    usernameColumn: {
      flex: 1,
      textAlign: "center",
      justifyContent: "center"
    },

    usernameColumnFiller: {
      flex: 1,
      marginTop: 45,
    },

    button: {
      height: 59,
      backgroundColor: "rgba(31,178,204,1)",
      borderRadius: 5,
      justifyContent: "center"
    },
    text2: {
      color: "rgba(255,255,255,1)",
      alignSelf: "center"
    },
    logoColumn: {
      flex: 1,
      justifyContent: "center",
      alignItems: "center",
    },
    logoColumnFiller: {
      flex: 1
    },
    footerTexts: {
      height: 14,
      flexDirection: "row",
      marginBottom: 36,
      marginLeft: 37,
      marginRight: 36
    },
    button2: {
      width: 104,
      height: 14,
      alignSelf: "flex-end"
    },
    createAccountFiller: {
      flex: 1
    },
    createAccount: {
      color: "rgba(255,255,255,0.5)"
    },
    button2Filler: {
      flex: 1,
      flexDirection: "row"
    },
    needHelp: {
      color: "rgba(255,255,255,0.5)",
      alignSelf: "flex-end",
      marginRight: -1
    },
    sensorTestImage: {
      marginLeft: "auto",
      marginRight: "auto",
      marginTop: 100,
    },
    sensorText: {
      color: "rgba(255,255,255,1)",
      alignSelf: "center"
    },
   
    saveTextWhite: {
      color: "#ffffff",
      alignItems: "center",
      textAlign: "center",
      marginTop: 2,
      fontSize: 18,
      
    },

    imageSize42: {
      width: 42,
      height: 42,
      //alignSelf: 'center'
    },

    saveBox: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'flex-end',
      height: 48,
      paddingHorizontal: 40,
  },

  button3: {
    backgroundColor: "#333333",
    width: 57,
    height: 30,
    textAlign: "center", 
    alignItems: "center",
    borderRadius: 8,
    marginLeft: 15,
  },

  spinnerTextStyle: {
    color: '#FFFFFF'
  },

  sensorPrompt: {
    marginHorizontal: 24,
    textAlign: 'center',
    fontSize: 20,
    lineHeight: 28,
    letterSpacing: -0.5,
    color: '#424852',
    marginTop: 30
  },

  sensorPrompt2: {
    marginHorizontal: 24,
    textAlign: 'center',
    fontSize: 20,
    lineHeight: 28,
    letterSpacing: -0.5,
    color: '#424852',
    marginBottom: 30
  },

  sensorWarning: {
    marginTop: 34,
    textAlign: 'center',
    fontSize: 15,
    letterSpacing: -0.5,
    color: '#424852',
  },

  successCheckBox: {
    position: 'absolute',
    top: -28,
    right: -28,
    width: 68,
    height: 68,
    borderRadius: 34,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#718cc7',
  },

  progressTimeView: {
    marginTop: 24,
  },

  progressTimeText: {
      textAlign: 'center',
      fontSize: 42,
      lineHeight: 45,
      letterSpacing: -0.5,
      color: '#424852',
  },

  checkBoxContainer: {
    flexDirection: 'row',
    marginTop: 25,
  },

  checkBoxText: {
    color: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 6
  },

  loadingAnimation: {
    marginTop: 20,
  },


});

export default styles;