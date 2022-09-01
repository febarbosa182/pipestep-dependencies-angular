package dynamic.dependencies

class Dependencies{
    def call (jenkins) {
        jenkins.podTemplate(
            containers: [
                jenkins.containerTemplate(
                    name: 'node', 
                    image: 'node:18', 
                    ttyEnabled: true, 
                    command: 'cat',
                    resourceLimitMemory: '1024Mi'
                )
            ],
            envVars: [
                jenkins.envVar(
                    key: 'NODE_OPTIONS',
                    value: '--max-old-space-size=768'
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
                    jenkins.sh label: "Prepare environment", script: """
                      apt update && apt upgrade -y
                      apt install sudo
                      sudo /bin/dd if=/dev/zero of=/var/swap.1 bs=1M count=1024
                      sudo /sbin/mkswap /var/swap.1
                      sudo /sbin/swapon /var/swap.1 || exit 0
                      """, returnStdout: true, returnStatus: false
                    jenkins.sh label: "Installing dependencies", script: "npm install --unsafe-perm=true --allow-root"
                    def packageJson = jenkins.readJSON file: 'package.json'
                    jenkins.env.APP_VERSION = packageJson.version
                }
            }
        }
    }
}
