# Instructions #


## Build ###
`$ gradle wrapper`

`$ ./gradlew clean build installDist`

## See Rules ##

`$  build/install/dqj/bin/dqj data/sample.csv`

## Execute Quality Checks ##
`$  build/install/dqj/bin/dqj --quality data/sample.csv`

You should see:

    Error in field 'CCType'(47) on line 927, content: 'FASTER CARD'
    Error in field 'Flyer Level'(95) on line 989, content: 'Bronzee'
    Error in field 'tlm_time'(99) on line 998, content: 'null'
