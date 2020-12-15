package br.com.easynvest.dependencies

class Dependencies{
    def call (jenkins, jenkinsParams) {
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
                }
            }
        }
    }
}