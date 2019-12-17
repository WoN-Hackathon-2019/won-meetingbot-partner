# Meeting-Bot Partner
This Bot demonstrates how to interact with the [Meeting-Bot](https://github.com/WoN-Hackathon-2019/won-meetingbot).

## How-To-Use
1. Create an atom with the tag `meetingapi`
2. Wait for the [Meeting-Bot](https://github.com/WoN-Hackathon-2019/won-meetingbot) to match your atom. 
3. Catch the `ConnectCommandEvent` and resend a `ConnectCommand` if the connection state is not `CONNECTED`
4. Next time you catch a `ConnectCommandEvent` the connection will be established. 
5. Send a `WonMessage` including a `VenueRequest`.
6. Catch the `MessageFromOtherAtomEvent` and extract the message from the event. 
7. Parse the message as a `VenueResponse`