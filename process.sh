function clean {
	fileForCleaning=$1
	cat $fileForCleaning | sed 's/\(.*\)\..*/\1/' | uniq &> cleanedCalls1.txt

	for line in $(cat cleanedCalls1.txt)
	do
	   start=$(echo $line | awk -F'->' '{print $1}' | sed 's/\(.*\)\..*/\1/')
	   end=$(echo $line | awk -F'->' '{print $2}')
	   echo $start"->"$end
	done &> cleaned.txt
}

function getAllNodes {
	cat cleaned.txt | awk -F'->' '{print $1}' 
	cat cleaned.txt | awk -F'->' '{print $2}'
}

function getJSON {
	echo "{ \"nodes\" : ["
	for node in $(getAllNodes | awk '{print $NF}' | sort | uniq)
	do
		echo "{ \"id\" : \"$node\","
		if [[ "$node" == "org.apache.kafka.common".* ]]
		then
			echo "\"type\": 1"
		elif [[ "$node" == "org.apache.kafka.server".* ]]
		then
			echo "\"type\": 2"
		elif [[ "$node" == "org.apache.kafka".* ]]
		then
			echo "\"type\": 3"
		else
			echo "\"type\": 4"
		fi
		echo " },"
	done
	echo "{}"
	echo "]"
	echo ", \"links\": ["
	cat cleaned.txt | awk -F'->' '{print "{\"source\": \"" $1 "\",\"target\": \"" $2"\"},"}'
	echo "{}"
	echo "]"

	echo "}"
}

if [ "$#" -gt 0 ]
then
	clean $1
fi
echo "const calls = " &> calls.json
getJSON >> calls.json
