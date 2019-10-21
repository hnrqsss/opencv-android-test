import { StyleSheet } from 'react-native'

export const styles = StyleSheet.create({
    imagePreview: {
      width: '100%',
      height: '100%',
    },
    
    container: {
      flex: 1,
      backgroundColor: '#000'
    },

    loadingContainer: { 
      flex: 1, 
      alignItems: 'center', 
      justifyContent: 'center',
      width: '100%',
      height: '100%',
      top: 0,
      left: 0,
      zIndex: 99,
      position: 'absolute',
      backgroundColor: '#000',
  },
    
})