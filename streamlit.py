import sys
from pathlib import Path
import os
import streamlit as st
from document_cropper.main import doc

st.title("Document Cropper")

pic=st.file_uploader("Upload a picture")

def crop_button():
  if st.button("Crop"):
    st.text(doc())
    

if pic is not None:
  st.subheader("Your picture")
  st.image(pic)
  crop_button()