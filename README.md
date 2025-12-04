
# render-applepay-java8 (Docker)
This repo contains a combined React frontend + Java 8 Spring Boot backend with Docker support for deployment on Render.com.

## Deploy steps (Render Docker web service)
1. Push this repository to Git (GitHub/GitLab) or upload as a tar/zip to your Render project.
2. Create a new Web Service on Render:
   - Environment: Docker
   - Dockerfile path: Dockerfile
   - Start command: (Dockerfile ENTRYPOINT runs the jar)
3. Set environment variables in Render dashboard:
   - STRIPE_SECRET_KEY
   - REACT_APP_STRIPE_PUBLISHABLE_KEY
   - STRIPE_WEBHOOK_SECRET
4. Render will build the Docker image and run the service.

## Local build and run (Docker)
1. Build image: `docker build -t render-applepay-java8 .`
2. Run container: `docker run -p 8080:8080 -e STRIPE_SECRET_KEY=sk_test_xxx -e REACT_APP_STRIPE_PUBLISHABLE_KEY=pk_test_xxx -e STRIPE_WEBHOOK_SECRET=whsec_xxx render-applepay-java8`
3. Visit http://localhost:8080

## Notes
- Make sure to verify your domain for Apple Pay in Stripe (domain association). Stripe Dashboard can host the domain verification file.
- This image builds frontend using node and backend using maven in multi-stage build. Ensure Docker has enough resources.
