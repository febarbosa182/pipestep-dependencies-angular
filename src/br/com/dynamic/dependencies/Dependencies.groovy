package br.com.dynamic.dependencies

class Dependencies{
    def call (jenkins) {
        jenkins.podTemplate(
            containers: [
                jenkins.containerTemplate(name: 'node', image: jenkins.env.CI_IMAGE, ttyEnabled: true, command: 'cat')
            ],
            yamlMergeStrategy: jenkins.merge(),
            workspaceVolume: jenkins.persistentVolumeClaimWorkspaceVolume(
                claimName: "pvc-${jenkins.env.JENKINS_AGENT_NAME}",
                readOnly: false
            )
        )

        {
            jenkins.node(jenkins.POD_LABEL){
                jenkins.container('node'){
                    jenkins.sh label: "Installing dependencies", script: "npm ci"
                    def packageJson = jenkins.readJSON file: 'package.json'
                    jenkins.env.APP_VERSION = packageJson.version
                }
            }
        }
    }
}