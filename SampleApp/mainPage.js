import React, { Component } from 'react';
import {SafeAreaView, StyleSheet, Text, View, Button, Alert} from 'react-native';

const buttonData = [
  {
  name : 'button1',
  content : 'touch!!'
  },
  {
    name : 'button2',
    content : 'touch!!'
  },
  {
    name : 'button3',
    content : 'touch!!!'
  },
  {
    name : 'button4',
    content : 'touch!!!!'
  }
]

class App extends Component {

  state = {
    counter : 0
  }
  
  onPress =() => {
    this.setState({
      counter: this.state.counter + 1
    })
  }

  render() {
    return (
  
      <SafeAreaView style={styles.container}>
  
        <View>
          {
            buttonData.map((buttonStyle, index) => (
              <View
                style={[
                  styles.buttonStyle,
                  (index === 0) && {borderTopWidth: 0},
                  (index % 2 === 1) && {backgroundColor: '#eee'}
                ]}>
                
                <Text>{buttonStyle.name}</Text>

                <Button title = {buttonStyle.content}
                  onPress = {this.onPress}></Button> 
                  
                </View>
            ))
          }
        </View>
  
          <View> 
            <Text>counter is {this.state.counter}</Text>
          </View>
  
      </SafeAreaView>
    );
  };

}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 3.5,
    backgroundColor: "#FFF0F8FF",
  },

  buttonStyle: {
    flexDirection: 'row', 
    justifyContent: 'space-between',
    alignItems: 'center',
    height: 50,
    paddingHorizontal: 20,
    borderTopWidth: 1,
    borderColor: '#000',
  }
});



export default App;

