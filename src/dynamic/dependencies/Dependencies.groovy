package dynamic.dependencies

class Dependencies{
    def call (jenkins) {
        jenkins.podTemplate(
            containers: [
                jenkins.containerTemplate(
                    name: 'node', 
                    image: 'cimg/node:18.7', 
                    ttyEnabled: true, 
                    command: '/bin/sh',
                    resourceRequestMemory: '2046'
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
                    jenkins.sh label: "Installing dependencies", script: "npm install"
                    def packageJson = jenkins.readJSON file: 'package.json'
                    jenkins.env.APP_VERSION = packageJson.version
                }
            }
        }
    }
}
