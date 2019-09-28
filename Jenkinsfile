#!/usr/bin/env groovy

// 需要配置的全局环境变量:
// DOCKER_REGISTRY_PUSH_ADDR
// ANSIBLE_MASTER

node {

    properties([parameters([choice(choices: ['deploy', 'rollback'], description: '''部署操作:
                    deploy : 部署新版本
                    rollback : 回滚旧版本''', name: 'OPERATION')])])

    checkout scm

    def imageNameBase = "${env.JOB_NAME}"

    def tagUrl = "http://${env.DOCKER_REGISTRY_PUSH_ADDR}/v2/${imageNameBase}/tags/list"
    def newTag = '0.0.1'

    def response = httpRequest customHeaders: [[maskValue: false, name: 'Authorization', value: 'Basic cm9vdDpyb290'], [maskValue: false, name: 'Accept', value: 'application/json']], responseHandle: 'STRING', url: tagUrl, validResponseCodes: '100:500'
    def content = readJSON text: response.content

    def tagsList = []

    // 如果为200且已经有tags,则根据已有tag生成新的tag
    if (response.status == 200 && content.tags != 'null') {
        tagsList = content.tags.sort().reverse()
        def numStr = tagsList[0].replace('.', '')
        def newVersionNum = Integer.parseInt(numStr) + 1
        newTag = "${(newVersionNum / 100).intValue()}.${(newVersionNum / 10).intValue() % 10}.${newVersionNum % 10}"
    }

    def imageFullName = "${imageNameBase}:${newTag}"

    if (params.OPERATION == 'deploy') {

        // maven打包
        docker.image('maven:3-alpine').inside('-v /root/.m2:/root/.m2') {
            stage('Maven package') {
                sh 'mvn clean package -Dmaven.test.skip=true'
            }
            stage('Maven test') {
                sh 'mvn test'
            }
        }

        // 生成docker镜像,上传到registry
        docker.withRegistry("https://${env.DOCKER_REGISTRY_PUSH_ADDR}", 'docker-registry-account') {
            stage('Docker ops') {
                def customImage = docker.build(imageFullName)
                customImage.push()
            }
        }
    } else {

        // 让用户选择需要回滚的版本号
        def rollbackVersion = input id: 'ROLLBACK_OPERATION', message: '请选择回滚的版本', ok: '确定', parameters: [choice(choices: tagsList, description: '', name: 'ROLLBACK_IMAGE_VERSION')]
        imageFullName = "${env.JOB_NAME}:${rollbackVersion}"
    }

    withCredentials([usernamePassword(credentialsId: 'ansible-master-account', passwordVariable: 'password', usernameVariable: 'username')]) {

        stage('Ansible deploy') {

            // 通过ansible部署相应的版本到服务器

            def remote = [:]
            remote.name = 'ansible-master'
            remote.host = "${ANSIBLE_MASTER}"
            remote.allowAnyHosts = true
            remote.user = "${username}"
            remote.password = "${password}"

            withCredentials([usernamePassword(credentialsId: 'docker-registry-account', passwordVariable: 'password', usernameVariable: 'username')]) {
                sshCommand remote: remote, command: "ansible-playbook -i /devops/ansible/hosts /devops/ansible/main.yml \
                                                     --tags my-netty-gateway \
                                                     -e IMAGE_NAME=${imageFullName}"
            }
        }
    }
}