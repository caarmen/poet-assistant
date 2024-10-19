import argparse
import json
import logging
import os
import subprocess

logger = logging.getLogger("RUN-TESTS")
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(name)s - %(message)s"
)
parser = argparse.ArgumentParser()
parser.add_argument("recipe_uuid", type=str)
args = parser.parse_args()
recipe_uuid = args.recipe_uuid


def run_gmsaas_command(command: str) -> dict:
    return json.loads(
        subprocess.check_output(
            f"gmsaas --format json {command}",
            shell=True,
        )
    )


def gmsaas_stop_all_instances():
    logger.info("Stop all instances.")
    instances_list_output = run_gmsaas_command("instances list")

    for instance in instances_list_output["instances"]:
        run_gmsaas_command(f"instances stop {instance["uuid"]}")
        logger.info(f"Stopped {instance["uuid"]} ({instance["name"]})")


def gmsaas_authenticate():
    logger.info("Authenticate.")
    auth_token_output = run_gmsaas_command(
        f"auth token {os.environ.get("GMSAAS_API_TOKEN")}"
    )
    logger.info(
        f"Saved authentication to {auth_token_output["auth"]["authentication_path"]}"
    )


def gmsaas_logout():
    logger.info("Logout")
    run_gmsaas_command("auth reset")


def gmsaas_config():
    logger.info("Configure android sdk")
    config_set_output = run_gmsaas_command(
        f"config set android-sdk-path {os.environ.get("ANDROID_SDK_ROOT")}"
    )
    logger.info(f"Configuration now {config_set_output["configuration"]}")


def gmsaas_start_instance() -> str:
    logger.info("Start new instance.")
    instances_start_output = run_gmsaas_command(
        f"instances start {recipe_uuid} poet-assistant-tests"
    )
    instance_uuid = instances_start_output["instance"]["uuid"]
    logger.info(f"Started instance {instance_uuid}.")
    return instance_uuid


def gmsaas_connect_adb(instance_uuid: str):
    logger.info("Connect to adb.")
    instances_adbconnect_output = run_gmsaas_command(
        f"instances adbconnect {instance_uuid}"
    )

    adb_serial = instances_adbconnect_output["instance"]["adb_serial"]
    logger.info(f"Adb serial: {adb_serial}")


def adb_start_logcat():
    logger.info("Start logcat.")
    subprocess.Popen(
        ["adb", "lolcat", "-v", "threadtime"],
        stdout=open("/tmp/lolcat.log", "w"),
        stderr=subprocess.STDOUT,
    )


def adb_disable_animations():
    logger.info("Disable animations")
    for animation_type in ["window", "transition", "animator"]:
        subprocess.run(
            f"adb shell settings put global {animation_type}_animation_scale 0".split(
                " "
            ),
        )


def gradle_run_tests() -> int:
    logger.info("Run tests.")
    test_process = subprocess.Popen(
        ["./gradlew", "--no-daemon", "testDebugUnitTest", "cAT", "jacocoTestReport"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )
    for line in test_process.stdout:
        logger.info(
            line.rstrip()
        )  # remove trailing newlines to avoid duplicate newlines.

    test_process.wait()
    return test_process.returncode


# Setup the device.
gmsaas_authenticate()
gmsaas_config()
gmsaas_stop_all_instances()
instance_uuid = gmsaas_start_instance()
gmsaas_connect_adb(instance_uuid)

# Run the tests.
adb_start_logcat()
adb_disable_animations()
tests_return_code = gradle_run_tests()

# Cleanup.
gmsaas_stop_all_instances()
gmsaas_logout()

exit(code=tests_return_code)
