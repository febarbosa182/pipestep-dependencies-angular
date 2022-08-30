package dynamic.dependencies

class Dependencies{
    def call (jenkins) {
        jenkins.podTemplate(
            containers: [
                jenkins.containerTemplate(
                    name: 'node', 
                    image: 'node:18-slim', 
                    ttyEnabled: true, 
                    command: 'sleep',
                    args: '99d',
                    resourceLimitMemory: '1536Mi'
                )
            ],
            envVars: [
                jenkins.envVar(
                    key: 'NODE_OPTIONS',
                    value: '--max-old-space-size=1024'
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
                    jenkins.sh label: "Installing dependencies", script: "npm ci"
                    def packageJson = jenkins.readJSON file: 'package.json'
                    jenkins.env.APP_VERSION = packageJson.version
                }
            }
        }
    }
}
