class VsQa < Formula
  desc "VS QA tool"
  homepage "https://github.com/VladislavSumin/vs-qa"
  url "https://github.com/VladislavSumin/vs-qa/releases/download/v%%VERSION%%/vs-qa-min.jar"
  sha256 "%%SHA256%%"
  version "%%VERSION%%"

  depends_on "openjdk@21"

  def install
    libexec.install "vs-qa-min.jar" => "vs-qa.jar"
    bin.write_jar_script libexec/"vs-qa.jar", "vs-qa"
  end
end
