# AndroidBioAuth
how use biometricPrompt

call Android biomatricPrompt for user authentication

using this code :

// floating biomatric authenticate message on your application


BioAuth.floatingAuth(object : BioAuth.AuthListener {
                override fun onAuthError() {
                  // todo something error  
                }
                override fun onAuthSuccess() {
                  // todo something success
                }
                override fun onAuthFailed() {
                  // todo something failed
                }
            })
           
         
