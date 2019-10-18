import { StyleSheet, Dimensions } from 'react-native'

const { height, width } = Dimensions.get('window')

export const styles = StyleSheet.create({
    container: {
        flex: 1,
        position: 'relative',
    },

    squareContainer: {
        height: height - 150,
        width,
        position: 'absolute',
        top: 0,
        left: 0,
        zIndex: 9,
        alignItems: 'center',
        justifyContent: 'center',
    },
    
    squareMask: {
        width: width - 115,
        height: height - 300,
        borderColor: '#FFF',
        borderWidth: 1.5,
        borderRadius: 8,
        marginTop: -20,
    },

    preview: {
        position: 'relative',
        flex: 1,
        justifyContent: 'flex-end',
        alignItems: 'center',
    },

    iconContainer: {
        width: 65,
        height: 65,
        backgroundColor: 'transparent',
        alignItems: 'center',
        justifyContent: 'center',
    },

    galleryButton: {
        width: 40,
        height: 40,
        borderRadius: 20,
        alignItems: 'center',
        justifyContent: 'center',
        borderColor: '#FFF',
        borderWidth: 1,
    },

    buttonContainer: {
        position: 'absolute',
        left: 0,
        bottom: 0,
        width: '100%',
        height: 150,
        backgroundColor: '#000',
        alignItems: 'center',
        justifyContent: 'space-around',
        flexDirection: 'row'
    },

    buttonBorder: {
        width: 65,
        height: 65,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'transparent',
        borderRadius: 32.5,
        borderColor: '#FFF',
        borderWidth: 1.2,
    },

    button: {
        width: 58,
        height: 58,
        backgroundColor: '#FFF',
        borderRadius: 29,
    }
})