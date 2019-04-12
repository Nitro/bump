# CycleWorks

Cycleworks is a service for crowd souring data on cycling journeys. Two observations inspired it's creation 
* Many policy documents (such as the recent [busconnects project](https://www.busconnects.ie/initiatives/core-bus-corridor-project/) rely on very simple sample cycling data e.g. a count of cyclist entering the city bounded by the canals on a small number of days 
* Most cyclists in Dublin have a smartphone in their pocket

Cycleworks aims to use the second observation to replace the first with a much richer and more complete data source on cycling journeys.
The hope is that by providing this richer source of data it will be easier to motivate changes to cycling infrastructure and cycyling policy in the city.

Some interesting data points we expect may be possible to capture via a smartphone app:
  * number of journeys
  * heatmap of journeys
  * heatmap of poor road condition
  * heatmap of hard braking events
  
# Challenges
One of the key challenges will be protecting individuals privacy so people are open to sharing their cycling journey info.
Some privacy topics already identified:
* Data is alway aggregated
.. * It is not possible to query an individuals data
* Data points are associated with a machine generated unique (per journey) id only
.. * no personal data is assocaited with this id e.g. no name, phone number or email address
* No data is shared on the first or last 100m of any journey
.. * in order to ensure starting point and destination are never shared
  
  
# Help
So far we have only built a very simple prototype. This is not our full time job and we are not android developers so we could do with some help:
* How to we collect data while smartphone in pocket or bag
* How to we identitfy start and end of cycling journeys
* How to we make the data available
* How to port from android to iphone
* ...
