pip install gmsaas==1.12.0
instance_name="poet-assistant-tests [${GITHUB_REF_NAME}-${GITHUB_SHA}] [${GITHUB_RUN_ID}-${GITHUB_RUN_ATTEMPT}]"
python ./scripts/gmsaas-run-tests.py ${GMSAAS_RECIPE_UUID} "${instance_name}"
