package com.smallsoho.mcplugin.image.webp

import com.smallsoho.mcplugin.image.Const
import com.smallsoho.mcplugin.image.models.Config
import com.smallsoho.mcplugin.image.utils.AndroidUtil
import com.smallsoho.mcplugin.image.utils.ImageUtil
import com.smallsoho.mcplugin.image.utils.LogUtil
import com.smallsoho.mcplugin.image.utils.Tools
import org.gradle.api.Project

class WebpUtils {

    static final int VERSION_SUPPORT_WEBP = 14
    static final int VERSION_SUPPORT_TRANSPARENT_WEBP = 18

    static boolean isPNGConvertSupported(Project project) {
        return AndroidUtil.getMinSdkVersion(project) >= VERSION_SUPPORT_WEBP
    }

    static boolean isTransparentPNGSupported(Project project) {
        return AndroidUtil.getMinSdkVersion(project) >= VERSION_SUPPORT_TRANSPARENT_WEBP
    }


    def static final TAG = 'Webp'

    def static formatWebp(File imgFile) {

        if (ImageUtil.isImage(imgFile)) {
            File webpFile = new File("${imgFile.getPath().substring(0, imgFile.getPath().indexOf("."))}.webp")
            LogUtil.log('execute cmd')
            def isSuccess = Tools.cmd("cwebp ${imgFile.getPath()} -o ${webpFile.getPath()} -quiet")
            if (isSuccess) {
                if (webpFile.length() < imgFile.length()) {
                    LogUtil.log("convert success ${imgFile.name}")
                    LogUtil.log(TAG, imgFile.getPath(), imgFile.length(), webpFile.length())
                    if (imgFile.exists()) {
                        imgFile.delete()
                    }
                } else {
                    LogUtil.log("${imgFile.name} convert success but too big")
                    //如果webp的大的话就抛弃
                    if (webpFile.exists()) {
                        webpFile.delete()
                    }
                }
            } else {
                LogUtil.log("convert fail ${imgFile.name}")
                if (webpFile.exists()) {
                    webpFile.delete()
                }
            }
        }

    }

    def static securityFormatWebp(File imgFile, Config config, Project project) {

        if (ImageUtil.isImage(imgFile)) {
            //png
            if (imgFile.getName().endsWith(Const.PNG)) {
                if (isPNGConvertSupported(project)) {
                    if (ImageUtil.isAlphaPNG(imgFile)) {
                        if (isTransparentPNGSupported(project)) {
                            formatWebp(imgFile)
                        } else {
                            LogUtil.log("mini sdk not support transparent webp convert ${imgFile.name} fail")
                        }
                    } else {
                        LogUtil.log("not transparent start convert ${imgFile.name}")
                        formatWebp(imgFile)
                    }
                } else {
                    LogUtil.log("mini sdk not support webp convert ${imgFile.name} fail")
                }
                //jpg
            } else if (imgFile.getName().endsWith(Const.JPG) || imgFile.getName().endsWith(Const.JPEG)) {
                if (config.isJPGConvert) {
                    formatWebp(imgFile)
                }
                //other
            } else {
                LogUtil.log(TAG, imgFile.getPath(), "don't convert")
            }
        }

    }

}