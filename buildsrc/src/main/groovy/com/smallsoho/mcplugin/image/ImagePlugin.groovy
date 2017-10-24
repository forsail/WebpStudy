package com.smallsoho.mcplugin.image

import com.android.build.gradle.AppPlugin
import com.smallsoho.mcplugin.image.compress.CompressUtil
import com.smallsoho.mcplugin.image.models.Config
import com.smallsoho.mcplugin.image.utils.FileUtil
import com.smallsoho.mcplugin.image.utils.ImageUtil
import com.smallsoho.mcplugin.image.utils.LogUtil
import com.smallsoho.mcplugin.image.webp.WebpUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class ImagePlugin implements Plugin<Project> {

    Project mProject
    Config mConfig

    @Override
    void apply(Project project) {

        FileUtil.instance.setRootDir(project.rootDir)

        mProject = project

        //判断是library还是application
        def hasAppPlugin = project.plugins.withType(AppPlugin)
        def variants = hasAppPlugin ? project.android.applicationVariants : project.android.libraryVariants

        //set config
        project.extensions.create('McImageConfig', Config)
        mConfig = project.McImageConfig

        def taskNames = project.gradle.startParameter.taskNames
        def isDebugTask = false
        def isContainAssembleTask = false
        for (int index = 0; index < taskNames.size(); ++index) {
            def taskName = taskNames[index]
            if (taskName.contains("assemble") || taskName.contains("resguard")) {
                if (taskName.toLowerCase().endsWith("debug") &&
                        taskName.toLowerCase().contains("debug")) {
                    isDebugTask = true
                }
                isContainAssembleTask = true
                break
            }
        }

        //export build clean
        if (!isContainAssembleTask) {
            LogUtil.log('export build clean')
            return
        }

        project.afterEvaluate {
            variants.all { variant ->

                if (!FileUtil.instance.getToolsDir().exists()) {
                    LogUtil.log('create the mctools dir in project root')
                    FileUtil.instance.getToolsDir().mkdir()
//                    throw new GradleException('You need put the mctools dir in project root')
                }

                def imgDir
                if (variant.productFlavors.size() == 0) {
                    imgDir = 'merged'
                } else {
                    imgDir = "merged/${variant.productFlavors[0].name}"
                }
                LogUtil.log("imgDir is $imgDir")

                //debug enable
                LogUtil.log("enableWhenDebug is ${mConfig.enableWhenDebug}")
                if (isDebugTask && !mConfig.enableWhenDebug) {
                    LogUtil.log('debug not run')
                    return
                }

                def processResourceTask = project.tasks.findByName("process${variant.name.capitalize()}Resources")
                def mcPicPlugin = "mcImage${variant.name.capitalize()}"
                project.task(mcPicPlugin) {
                    doLast {

                        LogUtil.log('plugin start')

                        String resPath = "${project.projectDir}/build/intermediates/res/${imgDir}/"

                        def dir = new File("${resPath}")

                        ArrayList<String> bigImgList = new ArrayList<>()

                        dir.eachDir() { channelDir ->
                            channelDir.eachDir { drawDir ->
                                def file = new File("${drawDir}")
                                if (file.name.contains('drawable') || file.name.contains('mipmap')) {
                                    file.eachFile { imgFile ->

                                        if (mConfig.isCheck && ImageUtil.isBigImage(imgFile, mConfig.maxSize)) {
                                            bigImgList.add(file.getPath() + file.getName())
                                        }
                                        if (mConfig.isCompress) {
                                            CompressUtil.compressImg(imgFile)
                                        }
                                        if (mConfig.isWebpConvert) {
                                            WebpUtils.securityFormatWebp(imgFile, mConfig, mProject)
                                        }

                                    }
                                }
                            }
                        }

                        if (bigImgList.size() != 0) {
                            StringBuffer stringBuffer = new StringBuffer("You have big Img!!!! \n")
                            for (int i = 0; i < bigImgList.size(); i++) {
                                stringBuffer.append(bigImgList.get(i))
                                stringBuffer.append("\n")
                            }
                            throw new GradleException(stringBuffer.toString())
                        }

                        LogUtil.log('plugin end')
                    }
                }

                //inject plugin
                project.tasks.findByName(mcPicPlugin).dependsOn processResourceTask.taskDependencies.getDependencies(processResourceTask)
                processResourceTask.dependsOn project.tasks.findByName(mcPicPlugin)
            }
        }
    }

}