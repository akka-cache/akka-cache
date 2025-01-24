import { ConfigurationParameters } from 'akka-cache';
import { Configuration } from 'akka-cache';

const cfgParams: ConfigurationParameters = {
    accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Nlc3Npb24uZmlyZWJhc2UuZ29vZ2xlLmNvbS9ha2thLWNhY2hlIiwib3JnIjoidHRvcmciLCJuYW1lIjoiSm9obiBEb2UiLCJzZXJ2aWNlTGV2ZWwiOiJmcmVlIn0.rds8orVxVz149ovTxxYzFIqGmSdWJUlHONem9avKBgQ',
//    basePath: "http://localhost:9001/cache"  // this is instead of accessToken if using the unsecured endpoint
}

// Pass the config Params to the Configuration
export const cfg = new Configuration(cfgParams)