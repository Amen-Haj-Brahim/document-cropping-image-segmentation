from flask import Flask

app = Flask(__name__)

@app.route("/crop")
def crop():
  print("crop")
  return "<p>crop module call</p>"

if __name__ == "__main__":
  app.run(debug=True)