source python-twitter/bin/activate
cd python-twitter/twitter
echo -e "\nGo to: http://localhost:5000/static/progress.html"
echo -e "\nPress Control-C when you want to exit.\n"
python FrontEnd.py

trap '{ echo "Killing twitter app!!"; deactivate; exit 0; }' INT
