require 'fileutils'

class Filesystem
  def initialize(path)
    @path = path
  end

  def absolute_path(path = '.')
    File.absolute_path(File.join(@path, path))
  end

  def executable(path, contents)
    path = absolute_path(path)
    FileUtils.mkdir_p(File.dirname(path))
    File.open(path, 'w+') { |io| io.print(contents) }
    File.chmod(0755, path)
  end

  def glob(pattern)
    Dir.glob(File.join(@path, pattern)).each do |path|
      yield relative_path(path), IO.read(path)
    end
  end

  def link(path, src)
    target = absolute_path(path)
    FileUtils.mkdir_p(File.dirname(target))
    FileUtils.ln(src, target, force: true)
  end

  def sandbox(*segments)
    Filesystem.new(File.join(@path, *segments))
  end

  def touch(path)
    path = absolute_path(path)
    FileUtils.mkdir_p(File.dirname(path))
    FileUtils.touch(path)
  end

  private

  def relative_path(path)
    path.slice(@path.length.next..-1) || ''
  end
end
