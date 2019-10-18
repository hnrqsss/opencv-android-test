import React from 'react'
import SvgIcon from 'react-native-svg-icon'
import svgs from '../../utils/svgs'


function Icon(props) {
    return <SvgIcon {...props} svgs={ svgs } />
}

export default Icon