node {
	properties([
		// Below line sets "Discard Builds more than 5"
		buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')), 
		
		// Below line triggers this job every minute
		pipelineTriggers([pollSCM('* * * * *')]),
		parameters([choice(choices: [
			'dev1.otabeksobirov.com.com', 
			'qa1.otabeksobirov.com.com', 
			'stage1.otabeksobirov.com.com', 
			'prod1.otabeksobirov.com.com'], 
			description: 'dev1.otabeksobirov.com', 
			name: '54.224.50.100')]), 
		])

		// Pulls a repo from developer
	stage("Pull Repo"){
		git   'https://github.com/farrukh90/cool_website.git'
	}
		//Installs web server on different environment
	stage("Install Prerequisites"){
		sh """
		ssh centos@${54.224.50.100}                 sudo yum install httpd -y
		"""
	}
		//Copies over developers files to different environment
	stage("Copy artifacts"){
		sh """
		scp -r *  centos@${ENVIR}:/tmp
		ssh centos@${54.224.50.100}                 sudo cp -r /tmp/index.html /var/www/html/
		ssh centos@${54.224.50.100}                 sudo cp -r /tmp/style.css /var/www/html/
		ssh centos@${54.224.50.100}				    sudo chown centos:centos /var/www/html/
		ssh centos@${54.224.50.100}				    sudo chmod 777 /var/www/html/*
		"""
	}
		//Restarts web server
	stage("Restart web server"){
		sh "ssh centos@${ENVIR}               sudo systemctl restart httpd"
	}

		//Sends a message to slack
	stage("Slack"){
		slackSend color: '#BADA55', message: 'Hello, World!'
	}
}