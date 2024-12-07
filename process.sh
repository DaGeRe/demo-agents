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
		echo "{ \"id\" : \"$node\" },"
	done
	echo "{}"
	echo "]"
	echo ", \"links\": ["
	cat cleaned.txt | awk -F'->' '{print "{\"source\": \"" $1 "\",\"target\": \"" $2"\"},"}'
	echo "{}"
	echo "]"

	echo "}"
}

clean $1
echo "const calls = " &> calls.json
getJSON >> calls.json
