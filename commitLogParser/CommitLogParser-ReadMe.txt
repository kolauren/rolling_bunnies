How to use CommitLogParser project

1. Get Commit logs from git:
	a. clone project
	b. run command in root folder: 
		git whatchanged --name-status --pretty=format:"commit, %h, %an, %at" > changes5.txt
2. Run CommitTxtToJSON with argument: < location of commit log from 1 >
