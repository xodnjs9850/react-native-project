import React from "react";
import { StyleSheet, View, Modal, ActivityIndicator } from "react-native";
import styles from '@utils/styles';

const Loader = (props) => {

    const { loading, ...attribues } = props;
    return (
        <Modal
            transparent={true}
            animationType={'none'}
            visible={loading}
            onRequestClose={() => {
                console.log('close modal');
            }}>
            <View style={styles.modalBackground}>
                <View style={styles.ActivityIndicatorWrapper}>
                    <ActivityIndicator 
                        animating={true}
                        color="#000000"
                        size="large"
                        style={styles.activityIndicator}
                    />
                </View>
            </View>
        </Modal>
    );
};

export default Loader;
