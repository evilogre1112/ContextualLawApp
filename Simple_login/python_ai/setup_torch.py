import os
import subprocess
import sys

# Các nguồn cài PyTorch chính thức
TORCH_INDEX = {
    "cuda": "https://download.pytorch.org/whl/cu130",
    "xpu": "https://download.pytorch.org/whl/xpu",
    "cpu": "https://download.pytorch.org/whl/cpu",
}


def get_gpu_names():
    """Đọc tên GPU trên Windows."""
    if os.name != "nt":
        return ""

    command = (
        "(Get-CimInstance Win32_VideoController "
        "| Select-Object -ExpandProperty Name) -join ';'"
    )

    result = subprocess.run(
        ["powershell.exe", "-NoProfile", "-Command", command],
        capture_output=True,
        text=True,
        check=False,
    )
    return result.stdout.strip()


def choose_backend(gpu_names):
    """Chọn backend theo GPU được Windows nhận diện."""
    names = gpu_names.lower()

    # Máy có cả NVIDIA và Intel thì ưu tiên NVIDIA
    if "nvidia" in names:
        return "cuda"

    # Chỉ chọn XPU cho GPU Intel Arc
    if "intel" in names and "arc" in names:
        return "xpu"

    return "cpu"


def torch_build_matches(backend):
    """Kiểm tra PyTorch hiện tại có đúng loại build cần dùng không."""
    try:
        import torch
    except ImportError:
        return False

    version = torch.__version__.lower()

    if backend == "cuda":
        return getattr(torch.version, "cuda", None) is not None or "+cu" in version

    if backend == "xpu":
        return "+xpu" in version or getattr(torch.version, "xpu", None) is not None

    # Có PyTorch rồi thì máy không nhận diện GPU phù hợp sẽ dùng bản đang có.
    return True


def install_torch(backend):
    command = [
        sys.executable,
        "-m",
        "pip",
        "install",
        "--upgrade",
        "torch",
        "--index-url",
        TORCH_INDEX[backend],
        "--retries",
        "10",
    ]

    print(f"Đang cài PyTorch backend: {backend}")
    subprocess.run(command, check=True)


def main():
    gpu_names = get_gpu_names()
    print("GPU Windows nhận diện:", gpu_names or "(không đọc được tên GPU)")

    backend = choose_backend(gpu_names)
    print("Backend được chọn:", backend)

    if torch_build_matches(backend):
        print("PyTorch phù hợp đã có, bỏ qua cài đặt.")
        return

    install_torch(backend)
    print("Cài PyTorch xong. Dịch vụ sẽ khởi động tiếp.")


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as error:
        print(f"Cài PyTorch thất bại, mã lỗi: {error.returncode}")
        sys.exit(error.returncode)