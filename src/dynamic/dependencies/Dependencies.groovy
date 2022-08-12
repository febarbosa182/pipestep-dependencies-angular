package dynamic.dependencies

class Dependencies{
    def call (jenkins) {
        jenkins.podTemplate(
            containers: [
                jenkins.containerTemplate(
                    name: 'node', 
                    image: 'node:18-alpine', 
                    ttyEnabled: true, 
                    command: '/bin/sh',
                    resourceRequestMemory: '1024'
                )
            ],
            yamlMergeStrategy: jenkins.merge(),
            workspaceVolume: jenkins.persistentVolumeClaimWorkspaceVolume(
                claimName: "pvc-workspace-${jenkins.env.JENKINS_AGENT_NAME}",
                readOnly: false
            )
        )

        {
            jenkins.node(jenkins.POD_LABEL){
                jenkins.container('node'){
                    jenkins.sh label: "Prepare for install",
                        returnStdout: true, 
                        script: """
                        sudo fallocate -l 1G /swapfile
                        sudo chmod 600 /swapfile
                        sudo mkswap /swapfile
                        sudo swapon /swapfile
                        echo "swapfile  none  swap  sw  0   0" >> /etc/fstab
                    """
                    jenkins.sh label: "Installing dependencies", script: "npm install"
                    def packageJson = jenkins.readJSON file: 'package.json'
                    jenkins.env.APP_VERSION = packageJson.version
                }
            }
        }
    }
}
