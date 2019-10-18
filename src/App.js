import React, { useEffect } from 'react'
import Orientation from 'react-native-orientation-locker'

import Camera from './screens/Camera'

function App() {
    useEffect(() => {
        Orientation.lockToPortrait()
    }, [])

    return (<Camera />)
}

export default App