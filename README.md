# abtest
the purpose of the project is to support ab testing of Recommendation System. <br>

##core
####input example
<pre>
{
	node : {type:"CATEGORY",name:"类目"},
	edges : {
		"x>50" : {
			node : {type:"ID",name:"ID"},
			edges : {
				"x>10" : "value1",
				"x<10" : "value2",
				"other" : "value3"
			}
		},
		"x<50" : {
			node : {type:"CITY",name:"城市"},
			edges : {
				"x>10":"value4",
				"x<10":"value5"
			}
		},
		"other" : {
			node : {type:"CITY",name:"城市"},
			edges : {
				"x>10":"value6",
				"x<10":"value7"
			}
		}
	}
}
</pre>

##output
ABTestTree
