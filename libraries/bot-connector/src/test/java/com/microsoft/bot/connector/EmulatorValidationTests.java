// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.EmulatorValidation;
import org.junit.Assert;
import org.junit.Test;

public class EmulatorValidationTests {
    @Test
    public void NoSchemeTokenIsNotFromEmulator() {
        Assert.assertFalse(EmulatorValidation.isTokenFromEmulator("AbCdEf123456"));
    }

    @Test
    public void OnePartTokenIsNotFromEmulator() {
        Assert.assertFalse(EmulatorValidation.isTokenFromEmulator("Bearer AbCdEf123456"));
    }

    @Test
    public void NoIssuerIsNotFromEmulator() {
        Assert.assertFalse(EmulatorValidation.isTokenFromEmulator("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZXNzYWdlIjoiSldUIFJ1bGVzISIsImlhdCI6MTQ1OTQ0ODExOSwiZXhwIjoxNDU5NDU0NTE5fQ.-yIVBD5b73C75osbmwwshQNRC7frWUYrqaTjTpza2y4"));
    }

    @Test
    public void ValidTokenSuccess() {
        String emToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImllX3FXQ1hoWHh0MXpJRXN1NGM3YWNRVkduNCIsImtpZCI6ImllX3FXQ1hoWHh0MXpJRXN1NGM3YWNRVkduNCJ9.eyJhdWQiOiI5YzI4NmUyZi1lMDcwLTRhZjUtYTNmMS0zNTBkNjY2MjE0ZWQiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIvIiwiaWF0IjoxNTY2NDIyMTY3LCJuYmYiOjE1NjY0MjIxNjcsImV4cCI6MTU2NjQyNjA2NywiYWlvIjoiNDJGZ1lKaDBoK0VmRDAvNnVWaUx6NHZuL25UK0RnQT0iLCJhcHBpZCI6IjljMjg2ZTJmLWUwNzAtNGFmNS1hM2YxLTM1MGQ2NjYyMTRlZCIsImFwcGlkYWNyIjoiMSIsImlkcCI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0L2Q2ZDQ5NDIwLWYzOWItNGRmNy1hMWRjLWQ1OWE5MzU4NzFkYi8iLCJ0aWQiOiJkNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIiLCJ1dGkiOiJPUXNSLWExUlpFS2tJcG9seUNJUUFBIiwidmVyIjoiMS4wIn0.J9qHO11oZlrpDU3MJcTJe3ErUqj0kw-ZQioYKbkwZ7ZpAx5hl01BETts-LOaE14tImqYqM2K86ZyX5LuAp2snru9LJ4S6-cVZ1_lp_IY4r61UuUJRiVUzn25kRZEN-TFi8Aj1iyL-ueeNr52MM1Sr2UUH73fwrferH8_0qa1IYc7affhjlFEWxSte0SN7iT5WaYK32d_nsgzJdZiCMZJPCpG39U2FYnSI8q7vvYjNbp8wDJc46Q4Jdd3zXYRgHWRBGL_EEkzzk9IFpHN7WoVaqNtgMiA4Vf8bde3eAS5lBBtE5VZ0F6fG4Qeg6zjOAxPBZqvAASMpgyDlSQMknevOQ";
        Assert.assertTrue(EmulatorValidation.isTokenFromEmulator(emToken));
    }
}
