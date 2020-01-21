require 'fileutils'

class Filesystem
  def initialize(path)
    @path = path
  end

  def absolute_path(path = '.')
    File.absolute_path(File.join(@path, path))
  end

  def exists?(path = '.')
    File.exist?(absolute_path(path))
  end

  def glob(pattern)
    Dir.glob(File.join(@path, pattern)).each do |path|
      yield relative_path(path), IO.read(path)
    end
    nil
  end

  def link(path, src)
    target = absolute_path(path)
    FileUtils.mkdir_p(File.dirname(target))
    FileUtils.ln(src, target, force: true)
    self
  end

  def mkpath(path = '.')
    FileUtils.mkdir_p(absolute_path(path))
    absolute_path(path)
  end

  def mtime(path = '.')
    File.mtime(absolute_path(path))
  end

  def sandbox(*segments)
    Filesystem.new(File.join(@path, *segments))
  end

  private

  def relative_path(path)
    path.slice(@path.length.next..-1) || ''
  end
end
